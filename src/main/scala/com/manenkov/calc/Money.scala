package com.manenkov.calc

import scala.math.pow

object Money {

  /** Future value.
   *
   * Future value is the value of an asset at a specific date. It measures
   * the nominal future sum of money that a given sum of money is "worth"
   * at a specified time in the future assuming a certain interest rate,
   * or more generally, rate of return; it is the present value multiplied
   * by the accumulation function. The value does not include corrections
   * for inflation or other factors that affect the true value of money
   * in the future. This is used in time value of money calculations.
   *
   * @param monthly     Today's amount of every month saves
   * @param months      Number of months
   * @param returnValue Returns, percent value per annum (3% as 0.03)
   * @return Value of the amount at a specific date
   * @see See [[https://en.wikipedia.org/wiki/Future_value]] for more
   */
  def futureValue(monthly: Double, months: Double, returnValue: Double): Double = {
    val monthlyReturnPercent = returnValue / 12.0
    (monthly * (1.0 + monthlyReturnPercent) * (pow(1.0 + monthlyReturnPercent, months) - 1.0)) / monthlyReturnPercent
  }

  /** Purchasing power.
   *
   * Purchasing power of amount after given number of months.
   *
   * @param amount         Amount
   * @param months         Number of months
   * @param inflationValue Inflation, percent value per annum (3% as 0.03)
   * @return Purchasing power of amount after given number of months
   */
  def purchasingPower(amount: Double, months: Double, inflationValue: Double): Double = {
    amount / pow(1.0 + inflationValue, months / 12.0)
  }

  /** Fixed monthly payments.
   *
   * Monthly payments to save given amount in today's value in given months.
   *
   * @param amount         Required amount
   * @param months         Number of months
   * @param inflationValue Inflation, percent value per annum (3% as 0.03)
   * @param returnValue    Returns, percent value per annum (3% as 0.03)
   * @return Value of monthly payments to save given amount in today's
   *         value in given months
   */
  def fixedMonthlyPayments(amount: Double, months: Int, inflationValue: Double, returnValue: Double): Double = {
    val fv = amount * pow(1.0 + inflationValue, months / 12.0)
    val r = returnValue / 12.0
    (fv * r) / ((1.0 + r) * (pow(1.0 + r, months) - 1.0))
  }

  /** Variable monthly payments with compensating for inflation.
   *
   * Monthly payments to save given amount in today's value in given months.
   * Inflation can be compensated for by increasing the monthly payments at the
   * same rate as inflation. This makes the payments equal in "value" terms.
   *
   * @param amount         Required amount
   * @param months         Number of months
   * @param inflationValue Inflation, percent value per annum (3% as 0.03)
   * @param returnValue    Returns, percent value per annum (3% as 0.03)
   * @param reverse        Reverse stream of values (from biggest to smallest)
   * @return Sequence of monthly payment values to save given amount in today's
   *         value in given months
   * @see See [[https://en.wikipedia.org/wiki/Effective_interest_rate#Calculation]] for more
   */
  def variableMonthlyPayments(amount: Double, months: Int, inflationValue: Double, returnValue: Double, reverse: Boolean = false): IndexedSeq[Double] = {
    val effectiveAnnualRate = pow(1.0 + inflationValue, 1.0 / 12.0) - 1.0

    def nthMonthPayment(month: Int): Double = {
      val fv = amount * pow(1.0 + inflationValue, months / 12.0)
      val r = returnValue / 12.0
      val x = (fv * (effectiveAnnualRate - r)) / ((1 + r) * (pow(1 + effectiveAnnualRate, months) - pow(1 + r, months)))
      x * pow(1 + effectiveAnnualRate, month)
    }

    val monthsSeq: IndexedSeq[Int] = if (reverse) (months - 1) to 0 by -1 else 0 until months
    for {
      month <- monthsSeq
    } yield nthMonthPayment(month)
  }

  /** Stream of monthly amount of savings.
   *
   * @param monthly        Today's amount of every month saves
   * @param inflationValue Inflation, percent value per annum (3% as 0.03)
   * @param returnValue    Returns, percent value per annum (3% as 0.03)
   * @return Stream of monthly amount of savings.
   */
  def savingsPerMonth(monthly: Double, inflationValue: Double, returnValue: Double): LazyList[(Int, Double)] = {
    lazy val stream: LazyList[(Int, Double)] = (0, 0.0) #:: stream.map { pair =>
      val effectiveAnnualRate = pow(1.0 + inflationValue, 1.0 / 12.0) - 1.0
      val month = pair._1
      val sum = pair._2
      (month + 1, (sum + monthly * pow(1.0 + effectiveAnnualRate, month)) * (1.0 + returnValue / 12.0))
    }
    stream.drop(1)
  }

  /** Calculate inflation rate by CPIs on current and previous periods.
   *
   * @param firstCPI  Initial consumer price index
   * @param secondCPI Next period consumer price index
   * @return Inflation rate as double (example: 3% as 0.03)
   * @see See [[https://en.wikipedia.org/wiki/Inflation]] for more
   * @see See [[https://en.wikipedia.org/wiki/Consumer_price_index]] for more
   */
  def inflationRate(firstCPI: Double, secondCPI: Double): Double = {
    (secondCPI - firstCPI) / firstCPI
  }
}
