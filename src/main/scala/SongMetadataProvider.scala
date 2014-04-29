import com.twitter.logging.Logger
import org.apache.hadoop.hbase.HBaseConfiguration
import org.kiji.express.flow.util.ResourceUtil._
import org.kiji.schema.layout.KijiTableLayout
import org.kiji.schema.{KijiTableReaderPool, KijiTable, KijiColumnName, KijiURI}
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONObject

class SongMetadataProvider(config: SongMetadataConfig, log: Logger) {
  var mTableURI: KijiURI = null
  var mColumnName: KijiColumnName = null
  var mProductsTable: KijiTable = null
  var mReaderPool: KijiTableReaderPool = null
  var mTableLayout: KijiTableLayout = null
  val mLogger = log

  // call setup at instantiation
  setup(config)

  def setup(config: SongMetadataConfig) {
    mLogger.debug("Setup SongMetadataProvider")

    mTableURI = KijiURI.newBuilder(config.songTable).build()
    mColumnName = mTableURI.getColumns.get(0)

    val conf = HBaseConfiguration.create()
    withKijiTable(mTableURI, conf) {
      table: KijiTable => {
        mProductsTable = table
        mReaderPool = KijiTableReaderPool.Builder.create()
            .withExhaustedAction(KijiTableReaderPool.Builder.WhenExhaustedAction.GROW)
            .withReaderFactory(table.getReaderFactory)
            .build()
        mTableLayout = table.getLayout
      }
    }
  }

  def getSongMetadata(songName: String): String = {
    "Loading from " + mTableURI.getTable
  }
}
