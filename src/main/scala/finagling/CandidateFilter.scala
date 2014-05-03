package finagling

import org.apache.avro.generic.GenericRecord
import finagling.CompareOp._

class CandidateFilter[K](val songPredicate: SongPredicate)(implicit ordering: Ordering[K]) extends CandidateSelect {
  require(songPredicate.getValue.isInstanceOf[K])

  /**
   * Same as the Scala predicate for filter. If the user said NO_OP, then it is a function that always returns true.
   */
  private val predicate: (GenericRecord) => Boolean = if (songPredicate.getOperator == NO_OP) {
    (input: GenericRecord) => true
  } else predicateFn[K]

  //private val accessMatcher = "[A-Za-z_]([A-Za-z0-9_])*\\[([0-9])*\\]"
  // this access matcher cannot index into arrays
  private val accessMatcher = "[A-Za-z_]([A-Za-z0-9_])*"

  /**
   * Access elements inside a (nested) Avro record.
   *
   * @param accessor string representing element to retrieve
   * @param what the avro object to retrieve from
   * @return the element corresponding to the accessor
   */
  private def avroAccessor(accessor: String, what: Object): Object = {
    val elements: Array[String] = accessor.split("\\.")
    // even when the string is empty, the (0) should return an empty string
    val nextElem: String = elements(0)

    if (!what.isInstanceOf[GenericRecord] && !nextElem.isEmpty) {
      throw new Exception("Cannot access primitive type")
    }

    val returnobj = if(nextElem.isEmpty) {
      what.asInstanceOf[Object]
    } else if (!nextElem.matches(accessMatcher)) {
      throw new Exception("Invalid accessor")
    } else {
      avroAccessor(elements.tail.mkString("."), what.asInstanceOf[GenericRecord].get(nextElem))
    }
    returnobj
  }

  /**
   * A function that enables the generation of a filter from a SongPredicate.
   *
   * @param input A generic record which contains the field/attribute to filter by.
   * @tparam K Type of the final field in the generic record that you are filtering by.
   * @return True if the field satisfies the criteria, false otherwise.
   */
  def predicateFn[K](input: GenericRecord)(implicit ordering: Ordering[K]): Boolean = {
    val obj: K = avroAccessor(songPredicate.getAttribute, input.asInstanceOf[Object]).asInstanceOf[K]
    val op: CompareOp = songPredicate.getOperator
    op match {
      case EQUAL => obj.equals(songPredicate.getValue)
      case NOT_EQUAL => !obj.equals(songPredicate.getValue)
      case GREATER => ordering.gt(obj, songPredicate.getValue.asInstanceOf[K])
      case GREATER_OR_EQUAL => ordering.gteq(obj, songPredicate.getValue.asInstanceOf[K])
      case LESS => ordering.lt(obj, songPredicate.getValue.asInstanceOf[K])
      case LESS_OR_EQUAL => ordering.lteq(obj, songPredicate.getValue.asInstanceOf[K])
    }
  }

  /**
   * The function that is called by the end user of this filter.
   *
   * @param candidates
   * @return
   */
  def filter(candidates: Seq[GenericRecord]): Seq[GenericRecord] = {
    super.filter(candidates, predicate)
  }
}