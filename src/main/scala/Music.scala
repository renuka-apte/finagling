import com.twitter.finatra.{FinatraServer, Request, Controller}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.kiji.schema.layout.KijiTableLayout
import org.kiji.schema.{KijiTableReaderPool, KijiTable, KijiColumnName, KijiURI}
import org.kiji.express.flow.util.ResourceUtil.withKijiTable

class Music extends Controller {

  get("/:song") { (request: Request) =>
    val name: String = request.routeParams.getOrElse("song", "default user")
    render.plain("hello " + name).toFuture
  }

}

object Music extends FinatraServer {
  val parser = new scopt.OptionParser[Config]("Music") {
    head("music")
    opt[String]('t', "table") action { (x, c) =>
      c.copy(songTable = x) } text "Kiji Table containing Song Metadata"
  }
  parser.parse(args, Config()) map { config =>
    val smp = SongMetadataProvider
    register(new Music())
  }
}