import com.twitter.logging.Logger
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.apache.hadoop.hbase.HBaseConfiguration
import org.kiji.express.flow.util.ResourceUtil._
import org.kiji.schema.{EntityId => JEntityId, KijiDataRequest, KijiRowData, KijiTableReader, KijiTableReaderPool, KijiTable, KijiColumnName, KijiURI}
import org.kiji.schema.layout.KijiTableLayout
import scala.collection.JavaConverters._

class SongProvider(config: SongMetadataConfig, log: Logger) {
  var mTableURI: KijiURI = null
  var mColumnName: KijiColumnName = null
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
    mLogger.debug("Setup SongMetadataProvider")

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

  def getSongMetadata(songName: String): String = {
    def reader: KijiTableReader = mReaderPool.borrowObject()

    val eid: JEntityId = mProductsTable.getEntityId(songName)
    val rows: KijiRowData = {
      doAndClose(reader) { tableReader: KijiTableReader =>
        tableReader.get(
          eid,
          KijiDataRequest.create(mFamilyName, mMetadataQualifier)
        )
      }
    }
    val gr = rows.getMostRecentValue(mFamilyName, mMetadataQualifier).asInstanceOf[GenericRecord]
    new String(gr.get("artist_name").asInstanceOf[Utf8].getBytes, "UTF-8")
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
