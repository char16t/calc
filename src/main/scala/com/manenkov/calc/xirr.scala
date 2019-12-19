package com.manenkov.calc

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class IncosistentCashFlowException extends RuntimeException

case class CashFlowFractionOfYear(var amount: Double, var years: Double)

case class CashFlowDates(var amount: Double, var date: LocalDateTime)

object CalculationWrapper {
  def XIRR(cashflows: Seq[CashFlowDates], decimals: Int = 4, maxRate: Double = 1000000): Double = {
    if (!cashflows.exists((x: CashFlowDates) => x.amount > 0.0)) throw new IncosistentCashFlowException
    if (!cashflows.exists((x: CashFlowDates) => x.amount < 0.0)) throw new IncosistentCashFlowException
    val precision = Math.pow(10.0, -decimals)
    val minRate = -(1.0 - precision)
    new XIRRCalculator(minRate, maxRate, toFractionOfYears(cashflows)).Calculate(precision, decimals)
  }

  private def toFractionOfYears(cashflows: Seq[CashFlowDates]): Seq[CashFlowFractionOfYear] = {
    val firstDate = cashflows.map(_.date).min
    cashflows.map(x => CashFlowFractionOfYear(x.amount, ChronoUnit.DAYS.between(firstDate.toLocalDate, x.date.toLocalDate) / 365.0))
  }
}

class XIRRCalculator(var lowRate: Double, var highRate: Double, cashFlow: Seq[CashFlowFractionOfYear]) {

  private var lowResult: Double = calcEquation(cashFlow, lowRate)
  private var highResult: Double = calcEquation(cashFlow, highRate)

  def Calculate(precision: Double, decimals: Int): Double = {
    if (Math.signum(lowResult).asInstanceOf[Int] == Math.signum(highResult).asInstanceOf[Int])
      throw new RuntimeException("Value cannot be calculated")
    val middleRate = (lowRate + highRate) / 2.0
    val middleResult = calcEquation(cashFlow, middleRate)
    if (Math.signum(middleResult).toInt == Math.signum(lowResult).asInstanceOf[Int]) {
      lowRate = middleRate
      lowResult = middleResult
    }
    else {
      highRate = middleRate
      highResult = middleResult
    }
    if (Math.abs(middleResult) > precision)
      Calculate(precision, decimals)
    else if (((highRate + lowRate) / 2.0).isNaN)
      Double.NaN
    else
      Math.round(((highRate + lowRate) / 2.0) * Math.pow(10.0, decimals)) / Math.pow(10.0, decimals)
  }

  private def calcEquation(cashflows: Seq[CashFlowFractionOfYear], interestRate: Double): Double = {
    cashflows.map(x => x.amount / Math.pow(1.0 + interestRate, x.years)).sum
  }
}