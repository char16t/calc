package com.manenkov.calc

import java.time.LocalDateTime

import org.scalactic.{Equality, TolerantNumerics}
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
    for (_ <- 0 until months) {
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

  it should "correctly calculate monthly savings sequence" in {
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

    val monthly: Double = 100.0
    val inflationValue: Double = 0.02
    val returnValue: Double = 0.1

    val savings = Money.savingsPerMonth(monthly, inflationValue, returnValue)
    savings.take(3).map(_._2).toList === List(100.83, 202.67, 305.53) should be (true)
  }

  it should "correctly calculate inflation rate" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.01)
    Money.inflationRate(1000.0, 1110.0) === 0.11 should be (true)
  }

  it should "correctly calculate CPI for single item" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.01)
    Money.consumerPriceIndex(2.5, 2.75) === 0.1 should be (true)
  }

  it should "correctly calculate CPI for multiple items" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.00000001)
    Money.consumerPriceIndex(LazyList((15,17.5), (10,12.5), (30,33), (25,27))) === 0.125 should be (true)
  }

  it should "correctly calculate XIRR" in {
    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.000001)
    val cf = Seq(
      (-997.78, LocalDateTime.of(2019, 3, 1, 0, 0)),
      (34.9, LocalDateTime.of(2019, 6, 19, 0, 0)),
      (34.9, LocalDateTime.of(2019, 12, 18, 0, 0)),
      (34.9, LocalDateTime.of(2020, 6, 17, 0, 0)),
      (34.9, LocalDateTime.of(2020, 12, 16, 0, 0)),
      (34.9, LocalDateTime.of(2021, 6, 16, 0, 0)),
      (1034.9, LocalDateTime.of(2021, 12, 15, 0, 0)),
    )
    val ex = 0.0778696
    Money.xirr(cf) === ex should be (true)

    val cf1 = Seq(
      (-1000.0, LocalDateTime.of(2017, 1, 1, 0, 0)),
      (1010.0, LocalDateTime.of(2018, 1, 1, 0, 0)),
    )
    val ex1 = 0.01
    Money.xirr(cf1) == ex1 should be (true)

    val cf2 = Seq(
      (-500.0, LocalDateTime.of(2017, 1, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 2, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 3, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 4, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 5, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 6, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 7, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 8, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 9, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 10, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 11, 1, 0, 0)),
      (-500.0, LocalDateTime.of(2017, 12, 1, 0, 0)),
      (6545.08, LocalDateTime.of(2018, 1, 1, 0, 0)),
    )
    val ex2 = 0.171156
    Money.xirr(cf2) === ex2 should be (true)
  }

  it should "throw exception if XIRR cannot be calculated (< 0)" in {
    val cf1 = Seq(
      (-1000.0, LocalDateTime.of(2017, 1, 1, 0, 0)),
    )
    assertThrows[IncosistentCashFlowException] {
      Money.xirr(cf1)
    }
  }

  it should "throw exception if XIRR cannot be calculated (> 0)" in {
    val cf1 = Seq(
      (1000.0, LocalDateTime.of(2017, 1, 1, 0, 0)),
    )
    assertThrows[IncosistentCashFlowException] {
      Money.xirr(cf1)
    }
  }
}
