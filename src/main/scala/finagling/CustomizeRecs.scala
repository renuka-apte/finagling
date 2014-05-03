package finagling

import com.twitter.finagle.{Http, Service}
import org.jboss.netty.handler.codec.http._
import com.twitter.util.Future
import com.twitter.logging.Logger

class CustomizeRecs(log: Logger) {
  val mClient: Service[HttpRequest, HttpResponse] =
    Http.newService("localhost:7070")

  def getRecs(songname: String): Future[HttpResponse] = {
    log.info("Get customized recs")
    val request =  new DefaultHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, "/recs/" + songname)
    mClient(request)
  }
}
