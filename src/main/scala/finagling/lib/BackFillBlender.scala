package finagling.lib

class BackFillBlender extends Blender{
  override def blend[T](candidates: Map[String, Seq[T]], numrecs: Int): Iterator[T] = {
    candidates.valuesIterator.flatten.take(numrecs)
  }
}
