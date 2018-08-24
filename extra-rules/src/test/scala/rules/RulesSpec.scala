package rules

import cats.data.Validated.Invalid
import io.tabmo.json.rules.{Rule, ValidationError}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.prop.PropertyChecks

trait RulesSpec extends WordSpec with PropertyChecks with Matchers {
  def executeRule[I, O](r: Rule[I, O], value: I) = r.rule.apply(value)
  def generateRuleError(error: String, args: String*) = Invalid(ValidationError(error, args: _*))
}
