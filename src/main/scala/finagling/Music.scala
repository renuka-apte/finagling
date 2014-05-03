package finagling

import com.twitter.finatra.FinatraServer
import com.twitter.app.Flag

/**
 * Created by renuka on 5/2/14.
 */
object Music extends FinatraServer {
  log.info("Music constructor starts.")
  val kijiURI: Flag[String] = flag("songtable", "", "Kiji table that contains song metadata")

  premain {
    log.info("premain called. Classpath:")
    log.info(System.getProperty("java.class.path") + "\n")
    val mSmp: SongProvider = if (kijiURI.isDefined) {
      new SongProvider(SongMetadataConfig(kijiURI()), log)
    } else null
    if (null != mSmp ) {
      register(new MusicMetadataController(mSmp))
      register(new MusicRecsController(mSmp))
    } else {
      throw new Exception("Need to specify flag songtable.")
    }
  }
}
