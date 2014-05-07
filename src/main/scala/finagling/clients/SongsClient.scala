package finagling.clients

import com.twitter.finagle.{Http, Service}
import org.jboss.netty.handler.codec.http._
import com.twitter.util.{Await, Future}
import com.twitter.logging.Logger
import java.nio.charset.Charset
import finagling.SongMetadata
import java.io.{ByteArrayInputStream, InputStream}
import org.apache.avro.io.{DecoderFactory, Decoder}
import org.apache.avro.specific.SpecificDatumReader

class SongsClient(log: Logger) {
  val mClient: Service[HttpRequest, HttpResponse] =
    Http.newService("localhost:7070")

  def getRecs(songname: String): Seq[String] = {
    log.info("Get customized recs")
    val request =  new DefaultHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, "/recs/" + songname)
    val response: HttpResponse = Await.result(mClient(request))
    val strres: String = response.getContent.toString(Charset.forName("UTF-8"))
    strres.split(",")
  }

  def getMetadata(songs: Seq[String]): Seq[SongMetadata] = {
    log.info("Get customized metadata")
    val request =  new DefaultHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, "/" + songs.mkString(","))
    val infoSongsHttp: HttpResponse = Await.result(mClient(request))
    val infoSongsStr: Seq[String] = infoSongsHttp
        .getContent.toString(Charset.forName("UTF-8")).split("\n")
    infoSongsStr.map { jsonMetadata: String =>
      val jsonInput: InputStream = new ByteArrayInputStream(jsonMetadata.getBytes("UTF-8"))
      val decoder: Decoder = DecoderFactory.get.jsonDecoder(SongMetadata.getClassSchema, jsonInput)
      val reader: SpecificDatumReader[SongMetadata] = new SpecificDatumReader[SongMetadata](SongMetadata.getClassSchema)
      reader.read(null, decoder)
    }
  }
}
