package finagling.controllers

import com.twitter.finatra.{Request, Controller}
import com.twitter.util.{Future, Await}
import java.nio.charset.Charset
import org.jboss.netty.handler.codec.http.HttpResponse
import finagling.providers.SongProvider
import finagling.clients.SongsClient
import finagling.{CompareOp, SongPredicate, SongMetadata}
import finagling.lib.CandidateFilter
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord}
import java.io.{IOException, ByteArrayOutputStream}
import org.apache.avro.io.{EncoderFactory, JsonEncoder}
import org.apache.hadoop.hbase.util.Bytes

class MusicRecsController(val mSmp: SongProvider) extends Controller {

  get("/recs/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    val res = mSmp.getSongRecs(name)
    render.plain(res).toFuture
  }

  get("/customizedrecs/:song") { (request: Request) =>
    log.info("Customized recs")
    val client = new SongsClient(log)
    val name: String = request.routeParams.getOrElse("song", "default user")

    // what Data Scientist writes
    val nextSongs = client.getRecs(name)
    val nextSongsMetadata: Seq[SongMetadata] = client.getMetadata(nextSongs)

    val filterPredicate = SongPredicate.newBuilder()
        .setAttribute("duration")
        .setOperator(CompareOp.GREATER)
        .setValue(240L)
        .build()
    val customRecs: Seq[GenericRecord] = CandidateFilter[Long](filterPredicate).filter(nextSongsMetadata)
    // end what Data Scientist writes

    val strres: Seq[String] = customRecs.map { record: GenericRecord =>
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
    log.info("HttpResponse is ready:\n" + strres.mkString("\n"))
    render.plain(strres.mkString("\n")).toFuture
  }
}