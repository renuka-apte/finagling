package finagling

import com.twitter.finatra.{Request, Controller}
import com.twitter.util.{Future, Await}
import java.nio.charset.Charset
import org.jboss.netty.handler.codec.http.HttpResponse

class MusicRecsController(val mSmp: SongProvider) extends Controller {

  get("/recs/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    val res = mSmp.getSongRecs(name)
    render.plain(res).toFuture
  }

  get("/customizedrecs/:song") { (request: Request) =>
    log.info("Customized recs")
    val name: String = request.routeParams.getOrElse("song", "default user")
    val custom: Future[HttpResponse] = new CustomizeRecs(log).getRecs(name)
    val response: HttpResponse = Await.result(custom)
    val strres: String = response.getContent.toString(Charset.forName("UTF-8"))
    val next_songs: Seq[String] = strres.split(",")
    log.info("HttpResponse is ready: " + strres)
    render.plain(strres).toFuture
  }
}