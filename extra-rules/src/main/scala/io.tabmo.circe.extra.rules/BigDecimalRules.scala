package io.tabmo.circe.extra.rules

import cats.data.Validated.Valid
import io.tabmo.json.rules.{GenericRules, Rule}

import scala.math.BigDecimal.RoundingMode

object BigDecimalRules extends GenericRules {

  def positive(errorCode: String = "error.positive")(implicit num: Numeric[BigDecimal]): Rule[BigDecimal, BigDecimal] =
    validateWith[BigDecimal](errorCode)(n => num.gt(n, num.zero))

  def max(max: BigDecimal, errorCode: String = "error.max.size"): Rule[BigDecimal, BigDecimal] =
    validateWith[BigDecimal](errorCode)(_ <= max)

  def min(min: BigDecimal, errorCode: String = "error.min.size"): Rule[BigDecimal, BigDecimal] =
    validateWith[BigDecimal](errorCode)(_ >= min)

  def scaleDecimal(nb: Int, roundingMode: BigDecimal.RoundingMode.RoundingMode = RoundingMode.HALF_UP): Rule[BigDecimal, BigDecimal] = Rule[BigDecimal, BigDecimal] { n =>
    Valid(n.setScale(nb, roundingMode))
  }

}