package finagling

import com.twitter.finatra.{Request, Controller}

class MusicMetadataController(val mSmp: SongProvider) extends Controller {

  get("/") { request =>
    render.plain("hi. enter a song using /:song").toFuture
  }

  get("/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    val res: String = mSmp.getSongMetadata(Seq(name))
    render.plain("Metadata for " + name + "is" + res).toFuture
  }

}