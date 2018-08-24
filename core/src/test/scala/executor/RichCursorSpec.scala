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

    "readArray method" should {
      "accept validated rules" in {
        case class TestDecodeArray(value: Array[String])

        val testDecodeArrayDecoder: Decoder[TestDecodeArray] = new Decoder[TestDecodeArray] {
          override def apply(c: HCursor): Result[TestDecodeArray] =
            c.readArray(valid).flatMap(v => Right(TestDecodeArray(v)))
        }

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeArrayDecoder.decodeJson(jsArr).right.get) { case TestDecodeArray(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readList method" should {
      "accept validated rules" in {
        case class TestDecodeList(value: List[String])

        val testDecodeArrayDecoder: Decoder[TestDecodeList] = new Decoder[TestDecodeList] {
          override def apply(c: HCursor): Result[TestDecodeList] =
            c.readList(valid).flatMap(v => Right(TestDecodeList(v)))
        }

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeArrayDecoder.decodeJson(jsArr).right.get) { case TestDecodeList(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readVector method" should {
      "accept validated rules" in {
        case class TestDecodeVector(value: Vector[String])

        val testDecodeVectorDecoder: Decoder[TestDecodeVector] = new Decoder[TestDecodeVector] {
          override def apply(c: HCursor): Result[TestDecodeVector] =
            c.readVector(valid).flatMap(v => Right(TestDecodeVector(v)))
        }

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeVectorDecoder.decodeJson(jsArr).right.get) { case TestDecodeVector(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readTraversable method" should {
      "accept validated rules" in {
        case class TestDecodeTraversable(value: Traversable[String])

        val testDecodeTraversableDecoder: Decoder[TestDecodeTraversable] = new Decoder[TestDecodeTraversable] {
          override def apply(c: HCursor): Result[TestDecodeTraversable] =
            c.readTraversable(valid).flatMap(v => Right(TestDecodeTraversable(v)))
        }

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeTraversableDecoder.decodeJson(jsArr).right.get) { case TestDecodeTraversable(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readSet method" should {
      "accept validated rules with an empty Json Array" in {
        case class TestDecodeSet(value: Set[String])

        val testDecodeSetDecoder: Decoder[TestDecodeSet] = new Decoder[TestDecodeSet] {
          override def apply(c: HCursor): Result[TestDecodeSet] =
            c.readSet(valid).flatMap(v => Right(TestDecodeSet(v)))
        }

        val emptyJsArray = Json.arr()

        inside(testDecodeSetDecoder.decodeJson(emptyJsArray).right.get) { case TestDecodeSet(value) =>
          value should contain theSameElementsAs Nil
        }
      }

      "accept validated rules with an not empty Json Array" in {
        case class TestDecodeSet(value: Set[String])

        val testDecodeSetDecoder: Decoder[TestDecodeSet] = new Decoder[TestDecodeSet] {
          override def apply(c: HCursor): Result[TestDecodeSet] =
            c.readSet(valid).flatMap(v => Right(TestDecodeSet(v)))
        }

        val notEmptyArray = ("1", "1", "2", "3", "2").asJson

        inside(testDecodeSetDecoder.decodeJson(notEmptyArray).right.get) { case TestDecodeSet(value) =>
          value should contain only ("1", "2", "3")
        }
      }

    }
  }

}
