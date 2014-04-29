import com.twitter.app.Flag
import com.twitter.finatra.{FinatraServer, Request, Controller}

class MusicController extends Controller {

  import Music._

  get("/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    //val res = mSmp.getSongMetadata(name)
    render.plain("hello! " + name).toFuture
  }

}

object Music extends FinatraServer {
  log.debug("Music start")
 /* val kijiURI: Flag[String] = flag("table", "", "Kiji table that contains song metadata")

  var mSmp: SongMetadataProvider = if (kijiURI.isDefined) {
    new SongMetadataProvider(SongMetadataConfig(kijiURI()), log)
  } else null
  register(new MusicController)*/
 val thingFlag = flag("thingEnabled", true, "Is the thing enabled")

  if (thingFlag()) {
    println("thing is enabled")
  }
}