package finagling

/**
 * Created by renuka on 5/1/14.
 */
trait CandidateRerank {
  /**
   * Transform the rank of a given list of items. A rank consists of a "position" in the resulting
   * list and a weight. The weight is calculated as (initial score) * (1 + sum(bonus_i))* (product(multiplier_i))
   * @param candidates
   * @param predicate
   * @param rankModifier
   * @tparam T
   */
  def rerank[T](candidates: Seq[T], predicate: (T) ⇒ Boolean, rankModifier: (Int, Int, Double))
}
