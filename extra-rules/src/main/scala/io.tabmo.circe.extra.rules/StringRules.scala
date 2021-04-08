package io.tabmo.circe.extra.rules

import cats.data.Validated.Valid
import io.tabmo.json.rules.{GenericRules, Rule}

object StringRules extends GenericRules {

  protected[this] val patternEmail = """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-\.]+([a-zA-Z]{2,})$\b""".r
  protected[this] val patternUrl = """(?i)\b(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;${}]*[-A-Za-z0-9+&@#/%=~_|${}]""".r

  def toLowerCase: Rule[String, String] = Rule((str: String) => { Valid(str.toLowerCase) })

  def toUpperCase: Rule[String, String] = Rule((str: String) => { Valid(str.toUpperCase) })

  val email: Rule[String, String] = email("error.email")
  def email(error: String): Rule[String, String] = pattern(patternEmail, error)

  val url: Rule[String, String] = url("error.url")
  def url(error: String) = pattern(patternUrl, error)

  def length(size: Int, errorCode: String = "error.length"): Rule[String, String] =
    validateWith[String](errorCode, size)(_.length == size)

  def maxLength(maxLength: Int, errorCode: String = "error.maximum.length"): Rule[String, String] =
    validateWith[String](errorCode, maxLength)(_.length <= maxLength)

  def minLength(minLength: Int, errorCode: String = "error.minimum.length"): Rule[String, String] =
    validateWith[String](errorCode, minLength)(_.length >= minLength)

  def isNotEmpty(errorCode: String = "error.is.not.empty"): Rule[String, String] =
    validateWith[String](errorCode)(!_.isEmpty)

  def notBlank(errorCode: String = "error.blank") =
    validateWith[String](errorCode) { !_.trim.isEmpty }
}
