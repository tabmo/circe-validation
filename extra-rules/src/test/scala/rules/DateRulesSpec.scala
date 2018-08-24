package rules

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

import cats.data.Validated.Valid
import io.tabmo.circe.extra.rules.DateRules
import org.scalacheck.Gen
import org.scalatest.{Inside}

import scala.util.Random

class DateRulesSpec extends RulesSpec with Inside {


  val simpleDateFormat = Seq(
    "yyyy-MM-dd",
    "yyyy-dd-MM",
    "MM-yyyy-dd",
    "dd-yyyy-MM",
    "dd-MM-yyyy",
    "MM-dd-yyyy"
  )

  val simpleDateTimeFormat = Seq(
    "dd LLLL uuuu",
    "dd uuuu LLLL",
    "LLLL dd uuuu",
    "LLLL uuuu dd",
    "uuuu LLLL dd",
    "uuuu dd LLLL"
  )

  val random = new Random()

  "Date rules" when {

    "date with pattern rules" should {
      "accept a simple date format" in {

        forAll(Gen.calendar) { value =>

          val randomPattern = random.shuffle(simpleDateFormat).head
          val format = new SimpleDateFormat(randomPattern)
          val dateStr = format.format(value.getTime)

          inside(executeRule(DateRules.dateWithPattern(randomPattern), dateStr)) { case Valid(date) =>
            val calendarDecoded = Calendar.getInstance()
            calendarDecoded.setTime(date)

            calendarDecoded.get(Calendar.YEAR)          should be (value.get(Calendar.YEAR))
            calendarDecoded.get(Calendar.MONTH)         should be (value.get(Calendar.MONTH))
            calendarDecoded.get(Calendar.DAY_OF_MONTH)  should be (value.get(Calendar.DAY_OF_MONTH))
          }
        }
      }

      "reject a date with the bad pattern" in {

        forAll(Gen.calendar) { value =>

          val format = new SimpleDateFormat("mm!yyyy!dd")
          val dateStr = format.format(value.getTime)

          executeRule(DateRules.date, dateStr) should ===(generateRuleError("error.expected.date", "yyyy-MM-dd"))
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

          inside(executeRule(DateRules.localDateWithPattern(randomPattern), localDateStr)) { case Valid(localDate) =>

            localDate.getYear       should be (zonedDate.getYear)
            localDate.getDayOfMonth should be (zonedDate.getDayOfMonth)
            localDate.getDayOfYear  should be (zonedDate.getDayOfYear)

          }
        }
      }

      "reject a localdate with the bad pattern" in {
        forAll(Gen.calendar) { value =>

          val randomPattern = random.shuffle(simpleDateTimeFormat).head
          val format = DateTimeFormatter.ofPattern(randomPattern)
          val localDateStr = format.format(value.toInstant.atZone(ZoneId.systemDefault()))

          executeRule(DateRules.localDate, localDateStr) should ===(generateRuleError("error.expected.localdate.format", "yyyy-MM-dd"))
        }
      }
    }

    "zoneDateTime with pattern rules" should {
      val zoneDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSZ"

      "accept a simple zoneDateTime format" in {

        forAll(Gen.calendar) { value =>

          val format = DateTimeFormatter.ofPattern(zoneDateTimePattern)
          val zonedDate = value.toInstant.atZone(ZoneId.of("UTC"))
          val zonedDateTimeStr = format.format(zonedDate)

          inside(executeRule(DateRules.zonedDateTimeWithPattern(zoneDateTimePattern), zonedDateTimeStr)) { case Valid(localDate) =>

            localDate.getDayOfMonth should be (zonedDate.getDayOfMonth)
            localDate.getHour       should be (zonedDate.getHour)
            localDate.getMinute     should be (zonedDate.getMinute)
            localDate.getSecond     should be (zonedDate.getSecond)

          }
        }
      }

      "reject a zoneDateTime with the bad pattern" in {

        forAll(Gen.calendar) { value =>

          val format = DateTimeFormatter.ofPattern(zoneDateTimePattern)
          val localDateStr = format.format(value.toInstant.atZone(ZoneId.systemDefault()))

          executeRule(DateRules.zonedDateTime, localDateStr) should ===(generateRuleError("error.expected.zoneddatetime.format", "yyyy-MM-dd'T'HH:mm:ssXXX"))
        }
      }
    }

    "timeFromLong transform correctly a long" in {
      inside(executeRule(DateRules.timeWithPattern, 1534922138669L)) { case Valid(date) =>
        date.getYear       should be (2018)
        date.getDayOfMonth should be (22)
        date.getHour       should be (7)
        date.getMinute     should be (15)
        date.getSecond     should be (38)
      }
    }

    "sqldate with pattern rules" should {
      "accept a simple date format" in {

        forAll(Gen.calendar) { value =>

          val randomPattern = random.shuffle(simpleDateFormat).head
          val format = new SimpleDateFormat(randomPattern)
          val dateStr = format.format(value.getTime)

          inside(executeRule(DateRules.sqlDateWithPattern(randomPattern), dateStr)) { case Valid(date) =>
            val calendarDecoded = Calendar.getInstance()
            calendarDecoded.setTime(date)

            calendarDecoded.get(Calendar.YEAR)          should be (value.get(Calendar.YEAR))
            calendarDecoded.get(Calendar.MONTH)         should be (value.get(Calendar.MONTH))
            calendarDecoded.get(Calendar.DAY_OF_MONTH)  should be (value.get(Calendar.DAY_OF_MONTH))
          }
        }
      }

      "reject a date with the bad pattern" in {

        forAll(Gen.calendar) { value =>

          val format = new SimpleDateFormat("mm!yyyy!dd")
          val dateStr = format.format(value.getTime)

          executeRule(DateRules.sqlDateR, dateStr) should ===(generateRuleError("error.expected.date", "yyyy-MM-dd"))
        }
      }
    }
  }
}
