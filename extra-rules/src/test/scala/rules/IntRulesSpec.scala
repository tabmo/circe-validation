package rules

import cats.data.Validated.Valid
import io.tabmo.circe.extra.rules.IntRules
import org.scalacheck.Gen

class IntRulesSpec extends RulesSpec {

  "Int rules" when {
    "a positive rule" should {

      "accept an Int with a positive value" in {
        forAll(Gen.chooseNum(1, 100)) { value =>
          executeRule(IntRules.positive(), value) should ===(Valid(value))
        }
      }

      "reject an Int with a negative value" in {
        forAll(Gen.chooseNum(-100, -1)) { value =>
          executeRule(IntRules.positive(), value) should ===(generateRuleError("error.positive"))
        }
      }
    }

    "a max value rule" should {

      "accept an Int which is under the maximum value" in {
        forAll(Gen.chooseNum(1, 49)) { value =>
          executeRule(IntRules.max(50), value) should ===(Valid(value))
        }
      }

      "reject an Int which is under the maximum value" in {
        forAll(Gen.chooseNum(2, 49)) { value =>
          executeRule(IntRules.max(1), value) should ===(generateRuleError("error.max.size"))
        }
      }
    }

    "a min value rule" should {

      "accept an Int which is upper the minimum value" in {
        forAll(Gen.chooseNum(2, 50)) { value =>
          executeRule(IntRules.min(1), value) should ===(Valid(value))
        }
      }

      "reject an Int which is upper the minimum value" in {
        forAll(Gen.chooseNum(1, 50)) { value =>
          executeRule(IntRules.min(51), value) should ===(generateRuleError("error.min.size"))
        }
      }
    }

  }
}
