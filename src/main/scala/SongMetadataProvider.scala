import org.apache.hadoop.hbase.HBaseConfiguration
import org.kiji.express.flow.util.ResourceUtil._
import org.kiji.schema.layout.KijiTableLayout
import org.kiji.schema.{KijiTableReaderPool, KijiTable, KijiColumnName, KijiURI}
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONObject

class SongMetadataProvider {
  def getSongMetadata(config: SongMetadataConfig): JSONObject = {
    val tableURI: KijiURI = KijiURI.newBuilder(config.songTable).build()
    val columnName: KijiColumnName = tableURI.getColumns.get(0)
    val familyName = columnName.getFamily
    val qualifier = columnName.getQualifier

    var mProductsTable: KijiTable = null
    var mReaderPool: KijiTableReaderPool = null
    var mTableLayout: KijiTableLayout = null

    val conf = HBaseConfiguration.create()
    withKijiTable(tableURI, conf) {
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
}
