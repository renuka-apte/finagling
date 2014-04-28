import com.twitter.finatra.{Request, Controller, FinatraServer}

class HelloWorld extends Controller {

  get("/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    render.plain("hello " + name).toFuture
  }

}

object HelloApp extends FinatraServer {
  register(new HelloWorld())
}