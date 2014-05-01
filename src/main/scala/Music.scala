import com.twitter.app.Flag
import com.twitter.finatra.FinatraServer

// TODO: We have no cleanup for the Kiji Reader Pool and such

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