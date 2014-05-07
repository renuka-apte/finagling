package finagling.lib

trait CandidateSelect {
  /**
   * Keep things that satisfy the predicate from the Seq.
   *
   * @param candidates
   * @param predicate
   * @tparam T
   * @return
   */
  def filter[T](candidates: Seq[T], predicate: (T) ⇒ Boolean): Seq[T] = {
    candidates.filter(predicate)
  }
}
