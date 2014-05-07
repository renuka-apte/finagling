package finagling.controllers

import com.twitter.finatra.{Request, Controller}

/**
 * Created by renuka on 5/2/14.
 */
class HelloWorld extends Controller {

  get("/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    render.plain("hello " + name).toFuture
  }

}
