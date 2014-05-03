package finagling

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.apache.avro.generic.GenericRecord

@RunWith(classOf[JUnitRunner])
class CandidateFilterSuite extends FunSuite {

  val firstTestProductMetaData = TestProductMetaData.newBuilder()
      .setProductId(1)
      .setCategory("bedroom")
      .setScore(TestScore.newBuilder().setRank(15).setWeight(10).build())
      .build()

  val secondTestProductMetaData = TestProductMetaData.newBuilder()
      .setProductId(2)
      .setCategory("bathroom")
      .setScore(TestScore.newBuilder().setRank(5).setWeight(10).build())
      .build()

  val testSeq: Seq[TestProductMetaData] = Seq(firstTestProductMetaData, secondTestProductMetaData)

  test("CandidateFilter filters EQUAL operator as expected") {
    val songpred = SongPredicate.newBuilder()
        .setAttribute("product_id")
        .setOperator(CompareOp.EQUAL)
        .setValue(1)
        .build()

    val cf = new CandidateFilter[Int](songpred)

    val res: Seq[GenericRecord] = cf.filter(testSeq)
    val expected: Seq[GenericRecord] = Seq(firstTestProductMetaData)
    assert(res == expected)
  }

  test("CandidateFilter filters NOT_EQUAL operator as expected") {
    val songpred = SongPredicate.newBuilder()
      .setAttribute("product_id")
      .setOperator(CompareOp.NOT_EQUAL)
      .setValue(1)
      .build()

    val cf = new CandidateFilter[Int](songpred)

    val res: Seq[GenericRecord] = cf.filter(testSeq)
    val expected: Seq[GenericRecord] = Seq(secondTestProductMetaData)
    assert(res == expected)
  }

  test("CandidateFilter filters NO_OP operator as expected") {
    val songpred = SongPredicate.newBuilder()
      .setAttribute("product_id")
      .setOperator(CompareOp.NO_OP)
      .setValue(1)
      .build()

    val cf = new CandidateFilter[Int](songpred)

    val res: Seq[GenericRecord] = cf.filter(testSeq)
    val expected: Seq[GenericRecord] = Seq(firstTestProductMetaData, secondTestProductMetaData)
    assert(res == expected)
  }

  test("CandidateFilter filters GREATER operator as expected") {
    val songpred = SongPredicate.newBuilder()
      .setAttribute("score.rank")
      .setOperator(CompareOp.GREATER)
      .setValue(5)
      .build()

    val cf = new CandidateFilter[Int](songpred)

    val res: Seq[GenericRecord] = cf.filter(testSeq)
    val expected: Seq[GenericRecord] = Seq(firstTestProductMetaData)
    assert(res == expected)
  }

  test("CandidateFilter filters GREATER_OR_EQUAL operator as expected") {
    val songpred = SongPredicate.newBuilder()
      .setAttribute("score.rank")
      .setOperator(CompareOp.GREATER_OR_EQUAL)
      .setValue(5)
      .build()

    val cf = new CandidateFilter[Int](songpred)

    val res: Seq[GenericRecord] = cf.filter(testSeq)
    val expected: Seq[GenericRecord] = Seq(firstTestProductMetaData, secondTestProductMetaData)
    assert(res == expected)
  }

  test("CandidateFilter filters LESS operator as expected") {
    val songpred = SongPredicate.newBuilder()
      .setAttribute("score.weight")
      .setOperator(CompareOp.LESS)
      .setValue(15)
      .build()

    val cf = new CandidateFilter[Int](songpred)

    val res: Seq[GenericRecord] = cf.filter(testSeq)
    val expected: Seq[GenericRecord] = Seq(firstTestProductMetaData, secondTestProductMetaData)
    assert(res == expected)
  }

  test("CandidateFilter filters LESS_OR_EQUAL operator as expected") {
    val songpred = SongPredicate.newBuilder()
      .setAttribute("category")
      .setOperator(CompareOp.LESS_OR_EQUAL)
      .setValue("clothing")
      .build()

    val cf = new CandidateFilter[String](songpred)

    val res: Seq[GenericRecord] = cf.filter(testSeq)
    val expected: Seq[GenericRecord] = Seq(firstTestProductMetaData, secondTestProductMetaData)
    assert(res == expected)
  }
}
