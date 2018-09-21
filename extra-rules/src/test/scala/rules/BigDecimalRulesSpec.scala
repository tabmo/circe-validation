package rules

import cats.data.Validated.Valid
import io.tabmo.circe.extra.rules.BigDecimalRules
import org.scalacheck.Gen

class BigDecimalRulesSpec extends RulesSpec {

  def genBigDecimal(min: Int, max: Int) = Gen.choose(min, max).map(BigDecimal.apply)

  "BigDecimal rules" when {
    "a positive rule" should {

      "accept an BigDecimal with a positive value" in {
        forAll(genBigDecimal(1, 100)) { value =>
          executeRule(BigDecimalRules.positive(), value) should ===(Valid(value))
        }
      }

      "reject an BigDecimal with a negative value" in {
        forAll(genBigDecimal(-100, -1)) { value =>
          executeRule(BigDecimalRules.positive(), value) should ===(generateRuleError("error.positive"))
        }
      }
    }

    "a max value rule" should {

      "accept an BigDecimal which is under the maximum value" in {
        forAll(genBigDecimal(1, 49)) { value =>
          executeRule(BigDecimalRules.max(BigDecimal(50.0)), value) should ===(Valid(value))
        }
      }

      "reject an BigDecimal which is under the maximum value" in {
        forAll(genBigDecimal(2, 49)) { value =>
          executeRule(BigDecimalRules.max(BigDecimal(1.0)), value) should ===(generateRuleError("error.max.size"))
        }
      }
    }

    "a min value rule" should {

      "accept an BigDecimal which is upper the minimum value" in {
        forAll(genBigDecimal(2, 50)) { value =>
          executeRule(BigDecimalRules.min(BigDecimal(1.0)), value) should ===(Valid(value))
        }
      }

      "reject an Int which is upper the minimum value" in {
        forAll(genBigDecimal(1, 50)) { value =>
          executeRule(BigDecimalRules.min(BigDecimal(51.0)), value) should ===(generateRuleError("error.min.size"))
        }
      }
    }

  }
}
