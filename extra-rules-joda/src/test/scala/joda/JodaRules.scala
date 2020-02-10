package joda

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Calendar

import cats.data.Validated.{Invalid, Valid}
import io.tabmo.extra.rules.joda.JodaRules
import io.tabmo.json.rules.{Rule, ValidationError}
import org.scalacheck.Gen
import org.scalatest.Inside
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.util.Random

class JodaRulesSpec extends AnyWordSpec with Matchers with Inside with ScalaCheckPropertyChecks {

  def executeRule[I, O](r: Rule[I, O], value: I) = r.rule.apply(value)
  def generateRuleError(error: String, args: String*) = Invalid(ValidationError(error, args: _*))

  val simpleDateFormat = Seq(
    "yyyy-MM-dd",
    "yyyy-dd-MM",
    "MM-yyyy-dd",
    "dd-yyyy-MM",
    "dd-MM-yyyy",
    "MM-dd-yyyy"
  )

  val simpleDateTimeFormat = Seq(
    "dd MMMM yyyy",
    "dd yyyy MMMM",
    "MMMM dd yyyy",
    "MMMM yyyy dd",
    "yyyy MMMM dd",
    "yyyy dd MMMM"
  )

  val random = new Random()

  "Joda Date rules" when {

    "date with pattern rules" should {
      "accept a simple date format" in {

        forAll(Gen.calendar) { value =>

          val randomPattern = random.shuffle(simpleDateFormat).head
          val format = new SimpleDateFormat(randomPattern)
          val dateStr = format.format(value.getTime)

          inside(executeRule(JodaRules.jodaDateWithPattern(randomPattern), dateStr)) { case Valid(date) =>

            date.getYear        should be (value.get(Calendar.YEAR))
            date.getMonthOfYear should be (value.get(Calendar.MONTH) + 1)
            date.getDayOfMonth  should be (value.get(Calendar.DAY_OF_MONTH))
          }
        }
      }

      "reject a date with the bad pattern" in {

        forAll(Gen.calendar) { value =>

          val format = new SimpleDateFormat("mm!yyyy!dd")
          val dateStr = format.format(value.getTime)

          executeRule(JodaRules.jodaDate, dateStr) should ===(generateRuleError("error.expected.jodadate.format", "yyyy-MM-dd"))
        }
      }
    }

    "localdate with pattern rules" should {
      "accept a simple date format" in {

        forAll(Gen.calendar) { value =>

          val randomPattern = random.shuffle(simpleDateTimeFormat).head
          val format = DateTimeFormatter.ofPattern(randomPattern)
          val zonedDate = value.toInstant.atZone(ZoneId.systemDefault())
          val localDateStr = format.format(zonedDate)

          inside(executeRule(JodaRules.jodaLocalDateWithPattern(randomPattern), localDateStr)) { case Valid(localDate) =>

            localDate.getYear         should be (zonedDate.get(ChronoField.YEAR_OF_ERA))
            localDate.getDayOfMonth   should be (zonedDate.getDayOfMonth)
            localDate.getMonthOfYear  should be (zonedDate.getMonthValue)

          }
        }
      }

      "reject a localdate with the bad pattern" in {
        forAll(Gen.calendar) { value =>

          val randomPattern = random.shuffle(simpleDateTimeFormat).head
          val format = DateTimeFormatter.ofPattern(randomPattern)
          val localDateStr = format.format(value.toInstant.atZone(ZoneId.systemDefault()))

          executeRule(JodaRules.jodaLocalDate, localDateStr) should be (generateRuleError("error.expected.jodadate.format", ""))
        }
      }
    }

    "timeFromLong transform correctly a long" in {
      inside(executeRule(JodaRules.jodaFromLong, 1534922138669L)) { case Valid(date) =>
        date.getYear            should be (2018)
        date.getDayOfMonth      should be (22)
        date.getMinuteOfHour    should be (15)
        date.getSecondOfMinute  should be (38)
      }
    }

    "ISODate rule" should {

      "accept a simple date format" in {
        val timeStr = "2018-12-04T08:12:43"

        inside(executeRule(JodaRules.isoDate(), timeStr)) { case Valid(date) =>
          val calendar = Calendar.getInstance()
          calendar.setTime(date)

          calendar.get(Calendar.YEAR)         should be (2018)
          (calendar.get(Calendar.MONTH) + 1)  should be (12) // Calendar.MONTH start from 0
          calendar.get(Calendar.DAY_OF_MONTH) should be (4)
          calendar.get(Calendar.HOUR)         should be (8)
          calendar.get(Calendar.MINUTE)       should be (12)
          calendar.get(Calendar.SECOND)       should be (43)
        }
      }

      "reject a malformed date" in {

        executeRule(JodaRules.isoDate(), "zdazdza") should ===(generateRuleError("error.expected.date.isoformat"))
      }
    }
  }
}
