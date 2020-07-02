package io.tabmo.json.rules

import cats.data.Validated.{Invalid, Valid}

import scala.util.matching.Regex

trait GenericRules {
  def validateWith[I](error: String, args: Any*)(f: I => Boolean): Rule[I, I] = { Rule[I, I](v => {
      if (f(v)) Valid(v)
      else Invalid(ValidationError(error, args: _*))
    })
  }

  def pattern(regex: Regex, error: String): Rule[String, String] = {
    validateWith(error)(str => regex.findFirstIn(str).isDefined)
  }
}
