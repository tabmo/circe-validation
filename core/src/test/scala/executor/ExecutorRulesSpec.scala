package executor

import cats.data.Validated.Valid
import cats.scalatest.ValidatedMatchers
import io.tabmo.json.rules.GenericRules
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ExecutorRulesSpec extends FlatSpec with Matchers with GenericRules with PropertyChecks with ValidatedMatchers {

  import io.tabmo.json.rules._

  def validStringtoString(msg: String = "valid"): Rule[String, String]   = validateWith[String](msg)(_ => true)
  def validStringtoInt(): Rule[String, Int] = Rule((str: String) => { Valid(str.toInt) })
  def invalidStringtoString(msg: String = "invalid"): Rule[String, String] = validateWith[String](msg)(_ => false)

  "Rule executor" should
    "return a valid result when the monoid composition of rules are valid" in {
      val ruleComposition = (validStringtoString() |+| validStringtoInt()).validate("1")
      ruleComposition should beValid[Int](1)
    }

    it should "return an invalid result when a least one rule is invalid" in {
      val ruleComposition = (validStringtoString() |+| invalidStringtoString()).validate("")
      ruleComposition should beInvalid[ValidationError](ValidationError("invalid"))
    }

    it should "shortcut first when the second rule is invalid" in {
      val ruleComposition = (invalidStringtoString("invalid first") |+| invalidStringtoString("invalid second")).validate("")
      ruleComposition.isInvalid should ===(true)
      ruleComposition should beInvalid[ValidationError](ValidationError("invalid first"))
    }

  "Rule executor monad" should
    "have a valid left identity law" in {

      forAll(Gen.alphaStr, Gen.alphaStr) { case (s1: String, s2: String) =>
        val f = (str: String) => Rule((str: String) => Valid(str.toUpperCase))
        val a = (str: String) => Valid(s1)
        val lhs = Rule(a).flatMap(f)

        val rhs = f(s1)

        lhs.validate(s2) should ===(rhs.validate(s2))
      }
    }

    it should "have a valid right identity law" in {

      forAll(Gen.alphaStr) { case (s1: String) =>
        val m = Rule((str: String) => { Valid(str.toUpperCase) })
        val lhs = m.flatMap(_ => Rule((str: String) => Valid(str.toUpperCase)))

        val rhs = m

        lhs.validate(s1) should ===(rhs.validate(s1))
      }

    }

    it should "have a valid associativity law" in {
      val m = Rule((str: String) => { Valid(str.toUpperCase) })
      val f = (str: String) => Rule((str: String) => Valid(str.toUpperCase))
      val g = (str: String) => Rule((str: String) => Valid(str.trim))

      val lhs = m.flatMap(f).flatMap(g)
      val rhs = m.flatMap(x => f(x).flatMap(g))

      forAll(Gen.alphaStr) { case (s1: String) =>
        lhs.validate(s1) should ===(rhs.validate(s1))
      }
    }

}
