Financial calculator

## Usage

Clone, build and publish library to local repository.
```bash
git clone https://github.com/char16t/calc
cd calc
sbt publishLocal
```

Add to your `build.sbt` (Scala 2.13.1 or higher):
```scala
libraryDependencies += "com.manenkov" %% "calc" % "0.1"
```

Library provides methods:

 * `futureValue` to calculate future value
 * `purchasingPower` to calculate purchasing power of amount after given number of months
 * `fixedMonthlyPayments` to calculate monthly payments to save given amount in today's value in given months
 * `variableMonthlyPayments` to calculate monthly payments to save given amount in today's value in given months (with compensating inflation)
 * `savingsPerMonth` to calculate stream of monthly amount of savings
 * `inflationRate` to calculate inflation rate by CPIs on current and previous periods
 * `consumerPriceIndex` to calculate consumer price index for single and multiple items
 * `xirr` to calculate internal rate of return for a schedule of cash flows that is not necessarily periodic
 
## Problem

You have a target amount `T` (in today's value, before inflation) that you would like to accumulate over `N` years. Every month you invest `X` with an expected return of `Y%`. Expected inflation is `Z%` per year. How can you calculate whether you investing enough? If that time is not enough, how long do you need to invest the money? How much should you invest monthly to meet the required deadline?

## Solution

If you save `x` every month, the future value (FV) is

![](http://latex.codecogs.com/gif.latex?FV%3D%5Csum_%7Bk%3D1%7D%5E%7Bn%7Dx%281%2Br%29%5Ek%3D%5Cfrac%7Bx%281%2Br%29%28%281%2Br%29%5En-1%29%7D%7Br%7D)

where

 * `n` is the number of months
 * `r` is the monthly rate of return
 
So if `Y%` return is 10% nominal interest compounded monthly

```scala
val r = 0.10/12
```

For example, over 3 months, saving $100 per month
```scala
val n  = 3
val x  = 100
val fv = (x * (1 + r) * (Math.pow(1 + r, n) - 1)) / r      // = 305.03
```

or using library

```scala
val monthly = 100
val months = 3
val returnValue = 0.1
val actual = Money.futureValue(monthly, months, returnValue)  // = 305.03
```

Checking the balance at the end of each month long-hand
```scala
val b1 = 100 * (1 + 0.1/12)                     // = 100.83        
val b2 = (b1 + 100) * (1 + 0.1/12)              // = 202.51
val b3 = (b2 + 100) * (1 + 0.1/12)              // = 305.03
```

The formula checks out.

So after 10 years, saving $100 per month
```scala
val n  = 120
val x  = 100 
val fv = (x * (1 + r) * (Math.pow(1 + r, n) - 1))/r        // = 20655.20
```

Discounting for inflation at, say, 2% per annum

```scala
fv / Math.pow(1 + 0.02, 10) = 16944.46
```

or using library

```scala
val amount = 20655.20
val months = 120
val inflationValue = 0.02
val actual = Money.purchasingPower(amount, months, inflationValue) // 16944.46
```

Your future saving of $20,655 would have the purchasing power of $16,944 today.

To work it backwards, if you want $100,000 in today's value in 10 years
```scala
val fv = 100000 * Math.pow(1 + 0.02, 10)                       // = 121899.44
val r  = 0.10/12
val n  = 120
val x  = (fv * r)/((1 + r) * (Math.pow(1 + r, n) - 1))         // = 590.16
```

or using library
```scala
val amount = 100_000.0
val months = 120
val inflationValue = 0.02
val returnValue = 0.1
val actual = Money.fixedMonthlyPayments(amount, months, inflationValue, returnValue) // = 590.16
```

You would need to save $590.16 each month.

***Compensating for inflation***

Inflation can be compensated for by increasing the monthly payments at the same rate as inflation. This makes the payments equal in 'value' terms.

Inflation is usually quoted as an [effective annual rate](https://en.wikipedia.org/wiki/Effective_interest_rate#Calculation), so with 2% (as before) the monthly rate is obtained like so
```scala
val i = Math.pow(1 + 0.02, 1/12) - 1                          // = 0.00165158
```
and the 3 month long-hand calculation becomes
```scala
val b1 = 100 * (1 + 0.1/12)                                   // = 100.83
val b2 = (b1 + 100 (1 + i)) * (1 + 0.1/12)                    // = 202.67
val b3 = (b2 + 100 * Math.pow(1 + i, 2)) * (1 + 0.1/12)       // = 305.53
```

or using library
```scala
val monthly: Double = 100.0
val inflationValue: Double = 0.02
val returnValue: Double = 0.1
val savings = Money.savingsPerMonth(monthly, inflationValue, returnValue)
// savings is List(100.83, 202.67, 305.53, ...)
```

This can be expressed as a formula

![](https://latex.codecogs.com/gif.latex?FV%3D%5Csum_%7Bk%3D1%7D%5E%7Bn%7Dx%281+i%29%5E%7Bn-k%7D%281+r%29%5Ek%3D%5Cfrac%7Bx%281+r%29%28%281+i%29%5En-%281+r%29%5En%29%7D%7Bi-r%7D)

![](https://latex.codecogs.com/gif.latex?%5Ctherefore%20x%3D%5Cfrac%7BFV%281-r%29%7D%7B%281+r%29%28%281+i%29%5En-%281+r%29%5En%29%7D)

Once again, to save $100,000 in today's value over ten years
```scala 
val fv = 100000 * Math.pow(1 + 0.02, 10)                                       // = 121899.44
val n  = 120
val i  = Math.pow(1 + 0.02, 1/12) - 1                                          // = 0.00165158
val r  = 0.10/12

val x  = (fv * (i - r))/((1 + r) * (Math.pow(1 + i, n) - Math.pow(1 + r, n)))  // = 542.84
```

The first payment is $542.84, and the payments increase like so
```scala
x * Math.pow(1 + i, 0)      // = 542.84 (month 1)
x * Math.pow(1 + i, 1)      // = 543.74 (month 2)
x * Math.pow(1 + i, 2)      // = 544.63 (month 3)
                            // ...
x * Math.pow(1 + i, 119)    // = 660.63 (month 120)
```

or using library
```scala
val amount = 100_000.0
val months = 120
val inflationValue = 0.02
val returnValue = 0.1
val payments = Money.variableMonthlyPayments(amount, months, inflationValue, returnValue, reverse = false)
// payments is List(542.84, 543.74, 544.63, ..., 660.63)
```

## Thanks

 * Chris Degnen for [answer on Stack Overflow](https://money.stackexchange.com/questions/117540/how-much-do-i-need-to-invest-monthly-to-accumulate-a-given-amount)
 * Nikolay Stanev for [XIRR demystifation](https://www.klearlending.com/en/Blog/Articles/XIRR-demystified)
 
## License

Source code licensed under Public Domain. See UNLICENSE file for details
