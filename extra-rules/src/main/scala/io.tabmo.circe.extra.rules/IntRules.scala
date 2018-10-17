package io.tabmo.circe.extra.rules

import io.tabmo.json.rules.{GenericRules, Rule}

object IntRules extends GenericRules {

  def positive(errorCode: String = "error.positive")(implicit num: Numeric[Int]): Rule[Int, Int] =
    validateWith[Int](errorCode)(n => num.gt(n, num.zero))

  def max(max: Int, errorCode: String = "error.max.size"): Rule[Int, Int] =
    validateWith[Int](errorCode)(_ <= max)

  def min(min: Int, errorCode: String = "error.min.size"): Rule[Int, Int] =
    validateWith[Int](errorCode)(_ >= min)

}