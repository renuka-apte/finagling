package finagling.lib

/**
 * Created by renuka on 5/2/14.
 */
trait Blender {
  def blend[T](candidates: Map[String, Seq[T]], numrecs: Int):Iterator[T]
}
