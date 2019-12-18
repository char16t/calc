package calc

import org.scalactic.{Equality, Equivalence, TolerantNumerics}
import org.scalatest._
import org.scalatest.matchers.should.Matchers

class MoneyTest extends FlatSpec with Matchers {

  it should "correctly calculate future value" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.00000000001)

    val monthly = 100
    val months = 3
    val returnValue = 0.1

    val actual = Money.futureValue(monthly, months, returnValue)
    var expected: Double = 0.0
    for (m <- 0 until months) {
      expected = (expected + monthly) * (1.0 + returnValue / 12.0)
    }
    actual === expected should be (true)
  }

  it should "correctly calculate purchasing power" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.01)

    val amount = 20655.20
    val months = 120
    val inflationValue = 0.02

    val actual = Money.purchasingPower(amount, months, inflationValue)
    val expected: Double = 16944.46

    actual === expected should be (true)
  }

  it should "correctly calculate fixed monthly payments to save given amount" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.01)

    val amount = 100_000.0
    val months = 120
    val inflationValue = 0.02
    val returnValue = 0.1

    val actual = Money.fixedMonthlyPayments(amount, months, inflationValue, returnValue)
    val expected: Double = 590.16

    actual === expected should be (true)
  }

  it should "correctly calculate variable monthly payments to save given amount" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.01)
    implicit val listEquality: Equality[List[Double]] = new Equality[List[Double]] {
      def areEqual(a: List[Double], b: Any): Boolean = {
        def areEqualRec(a: List[Double], b: List[Double]): Boolean = {
          (a, b) match {
            case (Nil, Nil) => true
            case (x :: xs, y :: ys) => x === y && areEquivalent(xs, ys)
            case _ => false
          }
        }
        b match {
          case daList: List[Double] => areEqualRec(a, daList)
          case _ => false
        }
      }
    }

    val amount = 100_000.0
    val months = 120
    val inflationValue = 0.02
    val returnValue = 0.1

    val actual = Money.variableMonthlyPayments(amount, months, inflationValue, returnValue)
    val actualReversed = Money.variableMonthlyPayments(amount, months, inflationValue, returnValue, reverse = true)

    actual.take(3).toList === List(542.84, 543.74, 544.63) should be (true)
    actual.last === 660.63 should be (true)
    actualReversed.head === actual.last should be (true)
  }
}
