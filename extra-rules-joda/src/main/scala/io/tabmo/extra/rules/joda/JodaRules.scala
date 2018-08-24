package io.tabmo.extra.rules.joda

import cats.data.Validated.{Invalid, Valid}
import io.tabmo.json.rules.{GenericRules, Rule, ValidationError}
import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

import scala.util.Try

object JodaRules extends GenericRules {

  /**
    * Rule for the `org.joda.time.DateTime` type.
    *
    * @param pattern a date pattern, as specified in `java.text.SimpleDateFormat`.
    * @param corrector a simple string transformation function that can be used to transform input String before parsing. Useful when standards are not exactly respected and require a few tweaks
    */
  def jodaDateWithPattern(pattern: String, corrector: String => String = identity, errorCode: String = "error.expected.jodadate.format"): Rule[String, DateTime] = Rule((str: String) => {

    val df = DateTimeFormat.forPattern(pattern)
    Try(df.parseDateTime(corrector(str)))
      .map(Valid.apply)
      .getOrElse(Invalid(ValidationError(errorCode, pattern)))
  })

  /**
    * the default implicit JodaDate reads
    * It uses the default date format: `yyyy-MM-dd`
    */
  def jodaDate: Rule[String, DateTime] = jodaDateWithPattern("yyyy-MM-dd")

  def jodaLocalDateWithPattern(pattern: String, corrector: String => String = identity,
    errorCode: String = "error.expected.jodadate.format"): Rule[String, LocalDate] = Rule((str: String) => {

    val df =
      if (pattern == "") ISODateTimeFormat.localDateParser
      else DateTimeFormat.forPattern(pattern)
    Try(org.joda.time.LocalDate.parse(corrector(str), df))
      .map(Valid.apply)
      .getOrElse(Invalid(ValidationError(errorCode, pattern)))
  })

  /**
    * The default implicit Rule for `org.joda.time.LocalDate`
    */
  def jodaLocalDate: Rule[String, LocalDate] = jodaLocalDateWithPattern("")

  /**
    * Default Rule for the `java.util.DateTime` type.
    */
  def jodaFromLong: Rule[Long, DateTime] = Rule((lg: Long) => {
    Valid(new org.joda.time.DateTime(lg))
  })

  /**
    * ISO 8601 Reads
    */
  def isoDate(errorCode: String = "error.expected.date.isoformat"): Rule[String, java.util.Date] = Rule((str: String) => {

    val parser = ISODateTimeFormat.dateOptionalTimeParser()
    Try(parser.parseDateTime(str).toDate())
      .map(Valid.apply)
      .getOrElse(Invalid(ValidationError(errorCode)))
  })

}
