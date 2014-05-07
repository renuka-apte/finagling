package finagling.controllers

import com.twitter.finatra.{Request, Controller}
import finagling.providers.SongProvider

class MusicMetadataController(val mSmp: SongProvider) extends Controller {

  get("/") { request =>
    render.plain("hi. enter a song using /:song").toFuture
  }

  get("/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    val songs: Seq[String] = name.split(",")
    val res: Seq[String] = mSmp.getSongMetadata(songs)
    render.plain(res.mkString("\n")).toFuture
  }

}