package finagling.servers

import com.twitter.finatra.FinatraServer
import finagling.controllers.HelloWorld

/**
 * Created by renuka on 5/2/14.
 */
object HelloApp extends FinatraServer {
  register(new HelloWorld())
}
