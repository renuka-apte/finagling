package finagling.providers

import com.twitter.logging.Logger
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord}
import org.apache.avro.util.Utf8
import org.apache.hadoop.hbase.HBaseConfiguration
import org.kiji.express.flow.util.ResourceUtil._
import org.kiji.schema.{EntityId => JEntityId, KijiDataRequest, KijiRowData, KijiTableReader, KijiTableReaderPool, KijiTable, KijiURI}
import org.kiji.schema.layout.KijiTableLayout
import scala.collection.JavaConverters._
import java.io.{IOException, ByteArrayOutputStream}
import org.apache.avro.io.{EncoderFactory, JsonEncoder}
import org.apache.hadoop.hbase.util.Bytes

class SongProvider(config: SongMetadataConfig, log: Logger) {
  var mTableURI: KijiURI = null
  var mProductsTable: KijiTable = null
  var mReaderPool: KijiTableReaderPool = null
  var mTableLayout: KijiTableLayout = null
  val mFamilyName: String = "info"
  val mRecsQualifier: String = "top_next_songs"
  val mMetadataQualifier: String = "metadata"
  val mLogger = log

  // call setup at instantiation
  setup(config)

  def setup(config: SongMetadataConfig) {
    mLogger.info("Setup SongMetadataProvider")

    mTableURI = KijiURI.newBuilder(config.songTable).build()

    val conf = HBaseConfiguration.create()
    withKijiTable(mTableURI, conf) {
      table: KijiTable => {
        mProductsTable = table
        mReaderPool = KijiTableReaderPool.Builder.create()
            .withExhaustedAction(KijiTableReaderPool.Builder.WhenExhaustedAction.GROW)
            .withReaderFactory(table.getReaderFactory)
            .build()
        mTableLayout = table.getLayout
      }
    }
  }

  /**
   * Return metadata for songs.
   *
   * @param songNames The names of the songs for which metadata is being requested.
   * @return A comma separated list of Json strings representing the metadata for the song.
   */
  def getSongMetadata(songNames: Seq[String]): Seq[String] = {
    def reader: KijiTableReader = mReaderPool.borrowObject()

    val eids: Seq[JEntityId] = songNames.map(mProductsTable.getEntityId(_))
    val rows: Seq[KijiRowData] = {
      doAndClose(reader) { tableReader: KijiTableReader =>
        tableReader.bulkGet(
          eids.asJava,
          KijiDataRequest.create(mFamilyName, mMetadataQualifier)
        ).asScala
      }
    }
    val metadataJsons: Seq[String] = rows.map { metadata: KijiRowData =>
      log.info(metadata.getEntityId.getComponentByIndex(0))
      val record: GenericRecord = metadata.getMostRecentValue(mFamilyName, mMetadataQualifier)
      //ToJson.toJsonString(record)
      try {
        val jsonOutputStream: ByteArrayOutputStream = new ByteArrayOutputStream
        val jsonEncoder: JsonEncoder = EncoderFactory.get.jsonEncoder(record.getSchema, jsonOutputStream)
        val writer: GenericDatumWriter[GenericRecord] = new GenericDatumWriter[GenericRecord](record.getSchema)
        writer.write(record, jsonEncoder)
        jsonEncoder.flush()
        log.info(Bytes.toString(jsonOutputStream.toByteArray))
        Bytes.toString(jsonOutputStream.toByteArray)
      } catch {
        case ioe: IOException => {
          throw new RuntimeException("Internal error: " + ioe)
        }
      }
    }
    metadataJsons
  }

  private def recToTuple(rec: GenericRecord): (String, Long) = {
    (new String(rec.get("song_id").asInstanceOf[Utf8].getBytes, "UTF-8"), rec.get("count").asInstanceOf[Long])
  }

  def getSongRecs(songName: String): String = {
    def reader: KijiTableReader = mReaderPool.borrowObject()

    val eid: JEntityId = mProductsTable.getEntityId(songName)
    val rows: KijiRowData = {
      doAndClose(reader) { tableReader: KijiTableReader =>
        tableReader.get(
          eid,
          KijiDataRequest.create(mFamilyName, mRecsQualifier)
        )
      }
    }
    // this is an array of SongCount
    val gr: List[GenericRecord] = rows.getMostRecentValue(mFamilyName, mRecsQualifier).asInstanceOf[GenericRecord]
      .get("topSongs").asInstanceOf[java.util.List[GenericRecord]].asScala.toList
    val recs: List[String] = gr.map (recToTuple)
      //reverse sort by the second element of tuple == count
      .sortBy(-_._2)
      .map(_._1)
    recs.mkString(",")
  }
}
