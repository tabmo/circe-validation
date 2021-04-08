package rules

import cats.data.Validated.Invalid
import io.tabmo.json.rules.{Rule, ValidationError}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

trait RulesSpec extends AnyWordSpec with Matchers {
  def executeRule[I, O](r: Rule[I, O], value: I) = r.rule.apply(value)
  def generateRuleError(error: String, args: Any*) = Invalid(ValidationError(error, args: _*))
}
