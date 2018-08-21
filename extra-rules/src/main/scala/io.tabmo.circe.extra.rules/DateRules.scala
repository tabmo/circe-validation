package io.tabmo.circe.extra.rules

import java.text.{ParseException, SimpleDateFormat}
import java.time._
import java.time.format.DateTimeFormatter
import java.util.Date

import cats.data.Validated.{Invalid, Valid}
import io.tabmo.json.rules.{GenericRules, Rule, ValidationError}

import scala.util.Try

/**
  * Trait imported by jto-validation
  */
object DateRules extends GenericRules {

  /**
    * Rule for the `java.util.Date` type.
    *
    * @param pattern a date pattern, as specified in `java.text.SimpleDateFormat`.
    * @param corrector a simple string transformation function that can be used to transform input String before parsing. Useful when standards are not exactly respected and require a few tweaks
    */
  def dateWithPattern(pattern: String, corrector: String => String = identity, errorCode: String = "error.expected.date"): Rule[String, Date] = Rule((str: String) => {
    def parseDate(input: String): Option[Date] = {
      val df = new SimpleDateFormat(pattern)
      df.setLenient(false)
      try {
        Some(df.parse(input))
      } catch {
        case _: ParseException => None
      }
    }

    parseDate(corrector(str)) match {
      case Some(d)  => Valid(d)
      case None     => Invalid(ValidationError(errorCode, pattern))
    }
  })

  /**
    * Default Rule for the `java.util.Date` type.
    * It uses the default date format: `yyyy-MM-dd`
    */
  def date: Rule[String, Date] = dateWithPattern("yyyy-MM-dd")

  /**
    * Rule for the `java.time.LocalDate` type.
    *
    * @param pattern a date pattern, as specified in `java.time.format.DateTimeFormatter`.
    * @param corrector a simple string transformation function that can be used to transform input String before parsing. Useful when standards are not exactly respected and require a few tweaks
    */
  def localDateWithPattern(pattern: String, corrector: String => String = identity, errorCode: String = "error.expected.localdate.format"): Rule[String, LocalDate] = Rule((str: String) => {
    val df = DateTimeFormatter.ofPattern(pattern)
    Try(LocalDate.parse(corrector(str), df))
      .map(Valid.apply)
      .getOrElse(Invalid(ValidationError(errorCode, pattern)))
  })

  /**
    * Default Rule for the `java.time.LocalDate` type.
    * It uses the default date format: `yyyy-MM-dd`
    */
  def localDate: Rule[String, LocalDate] = localDateWithPattern("yyyy-MM-dd")

  /**
    * Rule for the `java.time.ZonedDateTime` type.
    *
    * @param pattern a date pattern, as specified in `java.time.format.DateTimeFormatter`.
    * @param corrector a simple string transformation function that can be used to transform input String before parsing. Useful when standards are not exactly respected and require a few tweaks
    */
  def zonedDateTimeWithPattern(pattern: String, corrector: String => String = identity,
    errorCode: String = "error.expected.zoneddatetime.format"): Rule[String, ZonedDateTime] = Rule((str: String) => {

    val df = DateTimeFormatter.ofPattern(pattern)
    Try(ZonedDateTime.parse(corrector(str), df))
      .map(Valid.apply)
      .getOrElse(Invalid(ValidationError(errorCode, pattern)))
  })

  /**
    * Default Rule for the `java.time.ZonedDateTime` type.
    * It uses the default date format: `yyyy-MM-dd`
    */
  def zonedDateTime: Rule[String, ZonedDateTime] =
    zonedDateTimeWithPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

  /**
    * Default Rule for the `java.util.LocalDateTime` type from long.
    */
  def timeWithPattern: Rule[Long, LocalDateTime] = Rule((lg: Long) => {
    Valid(LocalDateTime.ofInstant(
      Instant.ofEpochMilli(lg),
      ZoneOffset.UTC))
  })

  /**
    * Rule for the `java.sql.Date` type.
    *
    * @param pattern a date pattern, as specified in `java.text.SimpleDateFormat`.
    * @param corrector a simple string transformation function that can be used to transform input String before parsing. Useful when standards are not exactly respected and require a few tweaks
    */
  def sqlDateWithPattern(pattern: String, corrector: String => String = identity): Rule[String, java.sql.Date] =
    dateWithPattern(pattern, corrector).map((d: java.util.Date) => new java.sql.Date(d.getTime))

  /**
    * The default implicit Rule for `java.sql.Date`
    */
  implicit def sqlDateR: Rule[String, java.sql.Date] = sqlDateWithPattern("yyyy-MM-dd")

}
