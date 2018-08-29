package rules

import cats.data.Validated.Valid
import io.tabmo.circe.extra.rules.StringRules
import org.scalacheck.Gen

import scala.util.Random

class StringRulesSpec extends RulesSpec {

  "String rules" when {
    "a minLength rule" should {

      "accept a String with an upper-sized cod" in {
        forAll(Gen.alphaStr.filter(_.length > 40)) { str =>
          executeRule(StringRules.minLength(40), str) should ===(Valid(str))
        }
      }

      "reject a String with an under-sized cod" in {
        forAll(Gen.alphaStr.filter(_.length < 40)) {str =>
          executeRule(StringRules.minLength(40), str) should ===(generateRuleError("error.minimum.length"))
        }
      }
    }

    "a maxLength rule" should {

      "accept a String with an under-sized cod" in {
        forAll(Gen.alphaStr.filter(_.length < 40)) { str =>
          executeRule(StringRules.maxLength(40), str) should ===(Valid(str))
        }
      }

      "reject a String with an upper-sized cod" in {
        forAll(Gen.alphaStr.filter(_.length > 40)) { str =>
          executeRule(StringRules.maxLength(40), str) should ===(generateRuleError("error.maximum.length"))
        }
      }
    }

    "a isEmpty rule" should {

      "accept a String doesn't empty" in {
        forAll(Gen.alphaStr.filter(_.length != 0)) { str =>
          executeRule(StringRules.isEmpty(), str) should ===(Valid(str))
        }
      }

      "reject a String with an upper-sized cod" in {
        executeRule(StringRules.isEmpty(), "") should ===(generateRuleError("error.is.empty"))
      }
    }

    "a length rule" should {

      "accept a String with the correct size" in {
        forAll(Gen.alphaStr) { str =>
          executeRule(StringRules.length(str.length), str) should ===(Valid(str))
        }
      }

      "reject a String with a bad size" in {
        forAll(Gen.alphaStr) { str =>
          executeRule(StringRules.length(str.length + 1), str) should ===(generateRuleError("error.length"))
        }
      }
    }

    "a notBlank rule" should {
      "accept a String with at least one character" in {
        forAll(Gen.alphaStr.filter(_.nonEmpty)) { str =>
          executeRule(StringRules.notBlank(), str) should ===(Valid(str))
        }
      }

      "reject a String with the space" in {
        executeRule(StringRules.notBlank(), "    ") should ===(generateRuleError("error.blank"))
      }
    }

    "a url pattern rule" should {

      val r = new Random()

      "accept an url String" in {
        val url = Seq(
          "http://aawsat.com",
          "http://mfa.gov.il/mfa/pages/default.aspx",
          "http://www.isa.gov.il/default.aspx?site=english",
          "https://ladeeni.",
          "http://ladeeni.",
          "https://www.tayara.co.il",
          "http://ladeeni",
          "https://ladeeni"
        )

        r.shuffle(url).map(url => executeRule(StringRules.url, url) should ===(Valid(url)))

      }

      "reject an url String" in {
        val url = Seq(
          "www.ladeeni.net",
          "",
          "ladeeni.net",
          "http",
          "https",
          "http://",
          "https://"
        )

        r.shuffle(url).map(url => executeRule(StringRules.url, url) should ===(generateRuleError("error.url")))

      }
    }

    "a url email rule" should {

      val r = new Random()

      "accept an email String" in {
        val url = Seq(
          "aawsat@ladeeni.com",
          "aawsat.tayara@ladeeni.com",
          "aawsat@ladeeni.tayara.com",
          "aawsat@ladeeni.tayara.er",
          "aawsat@ladeeni"
        )

        r.shuffle(url).map(url => executeRule(StringRules.email, url) should ===(Valid(url)))

      }

      "reject an url String" in {
        val url = Seq(
          "",
          "@",
          "aawsat@",
          "@ladeeni.com"
        )

        r.shuffle(url).map(url => executeRule(StringRules.email, url) should ===(generateRuleError("error.email")))

      }
    }

    "a toUpperCase rule" should {
      "accept a String and upper-case it" in {
        forAll(Gen.alphaStr) { str =>
          executeRule(StringRules.toUpperCase, str) should ===(Valid(str.toUpperCase))
        }
      }
    }

    "a toLowerCase rule" should {
      "accept a String and lower-case it" in {
        forAll(Gen.alphaStr) { str =>
          executeRule(StringRules.toLowerCase, str) should ===(Valid(str.toLowerCase))
        }
      }
    }
  }
}
