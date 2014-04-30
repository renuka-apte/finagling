import com.twitter.app.Flag
import com.twitter.finatra.{FinatraServer, Request, Controller}

class MusicController(val mSmp: SongMetadataProvider) extends Controller {

  get("/") { request =>
    render.plain("hi. enter a song using /:song").toFuture
  }

  get("/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    //val res = mSmp.getSongMetadata(name)
    render.plain("hello! " + name).toFuture
  }

}

object Music extends FinatraServer {
  log.info("Music constructor starts.")
  val kijiURI: Flag[String] = flag("songtable", "", "Kiji table that contains song metadata")

  premain {
    log.info("premain called")
    log.info(System.getProperty("java.class.path"))
    val mSmp: SongMetadataProvider = if (kijiURI.isDefined) {
      new SongMetadataProvider(SongMetadataConfig(kijiURI()), log)
    } else null
    if (null != mSmp ) {
      register(new MusicController(mSmp))
    } else {
      throw new Exception("Need to specify songtable")
    }
  }
}