package finagling

import com.twitter.finatra.FinatraServer

/**
 * Created by renuka on 5/2/14.
 */
object HelloApp extends FinatraServer {
  register(new HelloWorld())
}
