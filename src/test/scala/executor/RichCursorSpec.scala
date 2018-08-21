package executor

import io.circe.CursorOp.DownField
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, HCursor, Json}
import io.tabmo.json.rules.GenericRules
import org.scalacheck.Gen
import org.scalatest.{Inside, Matchers, WordSpec}
import org.scalatest.prop.PropertyChecks

class RichCursorSpec extends WordSpec with PropertyChecks with Matchers with GenericRules with Inside {

  import io.circe.syntax._
  import io.tabmo.json.rules._

  case class Person(firstName: String, lastName: String, list: Seq[String] = Nil)

  val personGenerator: Gen[Person] = {
    for {
      firstName <- Gen.alphaStr
      lastName  <- Gen.alphaStr
      list      <- Gen.listOf(Gen.alphaStr)
    } yield Person(firstName, lastName, list)
  }

  def personToJson(p: Person): Json = {
    Json.obj(
      "name" -> p.firstName.asJson,
      "lastName" -> p.lastName.asJson,
      "list" -> p.list.asJson
    )
  }

  def valid: Rule[String, String]   = validateWith[String]("valid")(_ => true)
  def invalid: Rule[String, String]   = validateWith[String]("invalid")(_ => false)

  "Rich cursor" when {
    "all rules are validated, return instance" in {

      val decodePerson: Decoder[Person] = new Decoder[Person] {
        override def apply(c: HCursor): Result[Person] = {
          for {
            name <- c.downField("name").read[String, String](valid |+| valid)
            lastName <- c.downField("lastName").as[String]
            list <- c.downField("list").readSeq(valid)
          } yield Person(name, lastName, list)
        }
      }

      forAll(personGenerator) { case p: Person =>
        inside(decodePerson.decodeJson(personToJson(p)).right.get) { case Person(firstName, name, list) =>
          firstName should be (p.firstName)
          name      should be (p.lastName)
          list      should be (p.list)
        }
      }
    }

    "a least one rule is invalid" in {
      val decodePerson: Decoder[Person] = new Decoder[Person] {
        override def apply(c: HCursor): Result[Person] = {
          for {
            name <- c.downField("name").read[String, String](valid |+| invalid)
            lastName <- c.downField("lastName").as[String]
          } yield Person(name, lastName)
        }
      }

      forAll(personGenerator) { case p: Person =>
        inside(decodePerson.decodeJson(personToJson(p)).left.get) { case DecodingFailure(error, errorField) =>
          error           should be ("invalid")
          errorField.head should be (DownField("name"))
        }
      }
    }

    "orElse method" should {
      "accept the first statement" in {
        val decodePerson: Decoder[Person] = new Decoder[Person] {
          override def apply(c: HCursor): Result[Person] = {
            for {
              name <- c.downField("name").readOrElse[String, String](valid)(invalid)
              lastName <- c.downField("lastName").as[String]
              list <- c.downField("list").readSeq(valid)
            } yield Person(name, lastName, list)
          }
        }

        forAll(personGenerator) { case p: Person =>
          inside(decodePerson.decodeJson(personToJson(p)).right.get) { case Person(firstName, name, list) =>
            firstName should be (p.firstName)
            name      should be (p.lastName)
            list      should be (p.list)
          }
        }
      }

      "reject the first statement and accept the second" in {
        val decodePerson: Decoder[Person] = new Decoder[Person] {
          override def apply(c: HCursor): Result[Person] = {
            for {
              name <- c.downField("name").readOrElse[String, String](invalid)(valid)
              lastName <- c.downField("lastName").as[String]
            } yield Person(name, lastName)
          }
        }

        forAll(personGenerator) { case p: Person =>
          inside(decodePerson.decodeJson(personToJson(p)).right.get) { case Person(firstName, name, _) =>
            firstName should be (p.firstName)
            name      should be (p.lastName)
          }
        }
      }

      "reject the first statement and the second" in {
        val decodePerson: Decoder[Person] = new Decoder[Person] {
          override def apply(c: HCursor): Result[Person] = {
            for {
              name <- c.downField("name").readOrElse[String, String](invalid)(invalid)
              lastName <- c.downField("lastName").as[String]
            } yield Person(name, lastName)
          }
        }

        forAll(personGenerator) { case p: Person =>
          inside(decodePerson.decodeJson(personToJson(p)).left.get) { case DecodingFailure(error, errorField) =>
            error           should be ("invalid")
            errorField.head should be (DownField("name"))
          }
        }
      }
    }
  }

}
