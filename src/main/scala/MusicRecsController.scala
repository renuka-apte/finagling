import com.twitter.finatra.{Request, Controller}

class MusicRecsController(val mSmp: SongProvider) extends Controller {

  get("/recs/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    val res = mSmp.getSongRecs(name)
    render.plain("You might like to listen to " + res).toFuture
  }

}