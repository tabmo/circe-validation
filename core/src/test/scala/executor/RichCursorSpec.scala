package executor

import cats.scalatest.EitherMatchers
import io.circe.CursorOp.DownField
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, HCursor, Json}
import io.tabmo.json.rules.GenericRules
import org.scalacheck.Gen
import org.scalatest.Inside
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class RichCursorSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers with GenericRules with Inside with EitherMatchers {

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

  def validRule[I]: Rule[I, I]     = validateWith[I]("valid")(_ => true)
  def invalidRule[I]: Rule[I, I]   = validateWith[I]("invalid")(_ => false)

  "Rich cursor" when {
    "all rules are validated, return instance" in {

      val decodePerson: Decoder[Person] = new Decoder[Person] {
        override def apply(c: HCursor): Result[Person] = {
          for {
            name <- c.downField("name").read[String, String](validRule |+| validRule)
            lastName <- c.downField("lastName").as[String]
            list <- c.downField("list").readSeq(validRule[String])
          } yield Person(name, lastName, list)
        }
      }

      forAll(personGenerator) { case p: Person =>
        inside(decodePerson.decodeJson(personToJson(p)).getOrElse(Person("", "", Seq("")))) { case Person(firstName, name, list) =>
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
            name <- c.downField("name").read[String, String](validRule |+| invalidRule)
            lastName <- c.downField("lastName").as[String]
          } yield Person(name, lastName)
        }
      }

      forAll(personGenerator) { case p: Person =>
        inside(decodePerson.decodeJson(personToJson(p)).swap.getOrElse(DecodingFailure("", List()))) { case DecodingFailure(error, errorField) =>
          error           should be ("invalid")
          errorField.head should be (DownField("name"))
        }
      }
    }

    "orElse method" should {
      "accept the first statement" in {
        val decodePerson: Decoder[Person] = (c: HCursor) => {
          for {
            name <- c.downField("name").readOrElse[String, String](validRule)(invalidRule)
            lastName <- c.downField("lastName").as[String]
            list <- c.downField("list").readSeq(validRule[String])
          } yield Person(name, lastName, list)
        }

        forAll(personGenerator) { case p: Person =>
          inside(decodePerson.decodeJson(personToJson(p)).getOrElse(Person("", "", Seq("")))) { case Person(firstName, name, list) =>
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
              name <- c.downField("name").readOrElse[String, String](invalidRule)(validRule)
              lastName <- c.downField("lastName").as[String]
            } yield Person(name, lastName)
          }
        }

        forAll(personGenerator) { case p: Person =>
          inside(decodePerson.decodeJson(personToJson(p)).getOrElse(Person("", "", Seq("")))) { case Person(firstName, name, _) =>
            firstName should be (p.firstName)
            name      should be (p.lastName)
          }
        }
      }

      "reject the first statement and the second" in {
        val decodePerson: Decoder[Person] = (c: HCursor) => {
          for {
            name <- c.downField("name").readOrElse[String, String](invalidRule)(invalidRule)
            lastName <- c.downField("lastName").as[String]
          } yield Person(name, lastName)
        }

        forAll(personGenerator) { case p: Person =>
          inside(decodePerson.decodeJson(personToJson(p)).swap.getOrElse(DecodingFailure("", List()))) { case DecodingFailure(error, errorField) =>
            error           should be ("invalid")
            errorField.head should be (DownField("name"))
          }
        }
      }
    }

    "readOpt method" should {
      case class TestDecodeOpt(value: Option[String])

      def strToJsString(str: String) = Json.fromString(str)

      "return Some(result), with a valid rule" in {

        val testDecodeOptDecoder: Decoder[TestDecodeOpt] = (c: HCursor) => c.readOpt(validRule[String]).flatMap(v => Right(TestDecodeOpt(v)))

        forAll(Gen.alphaStr) { case valueGen =>
          val jsStr = strToJsString(valueGen)

          inside(testDecodeOptDecoder.decodeJson(jsStr).getOrElse(TestDecodeOpt(None))) { case TestDecodeOpt(value) =>
            value should be (Some(valueGen))
          }
        }
      }

      "reject invalid rule" in {

        val testDecodeOptDecoder: Decoder[TestDecodeOpt] = (c: HCursor) => c.readOpt(invalidRule[String]).flatMap(v => Right(TestDecodeOpt(v)))

        forAll(Gen.alphaStr) { case valueGen =>
          val jsStr = strToJsString(valueGen)

          testDecodeOptDecoder.decodeJson(jsStr) should be (left)
        }
      }

      "return None value when the value is null" in {
        val testDecodeOptDecoder: Decoder[TestDecodeOpt] = (c: HCursor) => c.readOpt(invalidRule[String]).flatMap(v => Right(TestDecodeOpt(v)))

        inside(testDecodeOptDecoder.decodeJson(Json.Null).getOrElse(TestDecodeOpt(None))) { case TestDecodeOpt(value) =>
          value should be (None)
        }
      }
    }

    "readArray method" should {
      "accept validated rules" in {
        case class TestDecodeArray(value: Array[String])

        val testDecodeArrayDecoder: Decoder[TestDecodeArray] = (c: HCursor) => c.readArray(validRule[String]).flatMap(v => Right(TestDecodeArray(v)))

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeArrayDecoder.decodeJson(jsArr).getOrElse(TestDecodeArray(Array()))) { case TestDecodeArray(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readList method" should {
      "accept validated rules" in {
        case class TestDecodeList(value: List[String])

        val testDecodeArrayDecoder: Decoder[TestDecodeList] = (c: HCursor) => c.readList(validRule[String]).flatMap(v => Right(TestDecodeList(v)))

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeArrayDecoder.decodeJson(jsArr).getOrElse(TestDecodeList(List()))) { case TestDecodeList(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readVector method" should {
      "accept validated rules" in {
        case class TestDecodeVector(value: Vector[String])

        val testDecodeVectorDecoder: Decoder[TestDecodeVector] = (c: HCursor) => c.readVector(validRule[String]).flatMap(v => Right(TestDecodeVector(v)))

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeVectorDecoder.decodeJson(jsArr).getOrElse(TestDecodeVector(Vector()))) { case TestDecodeVector(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readIterable method" should {
      "accept validated rules" in {
        case class TestDecodeIterable(value: Iterable[String])

        val testDecodeIterableDecoder: Decoder[TestDecodeIterable] = (c: HCursor) => c.readIterable(validRule[String]).flatMap(v => Right(TestDecodeIterable(v)))

        def listToJsArray(l: Seq[String]) = Json.arr(l.map(_.asJson): _*)

        forAll(Gen.listOf(Gen.alphaStr)) { strList =>
          val jsArr = listToJsArray(strList)

          inside(testDecodeIterableDecoder.decodeJson(jsArr).getOrElse(TestDecodeIterable(Iterable()))) { case TestDecodeIterable(value) =>
            value should contain theSameElementsAs strList
          }
        }
      }
    }

    "readSet method" should {
      "accept validated rules with an empty Json Array" in {
        case class TestDecodeSet(value: Set[String])

        val testDecodeSetDecoder: Decoder[TestDecodeSet] = (c: HCursor) => c.readSet(validRule[String]).flatMap(v => Right(TestDecodeSet(v)))

        val emptyJsArray = Json.arr()

        inside(testDecodeSetDecoder.decodeJson(emptyJsArray).getOrElse(TestDecodeSet(Set()))) { case TestDecodeSet(value) =>
          value should contain theSameElementsAs Nil
        }
      }

      "accept validated rules with an not empty Json Array" in {
        case class TestDecodeSet(value: Set[String])

        val testDecodeSetDecoder: Decoder[TestDecodeSet] = (c: HCursor) => c.readSet(validRule[String]).flatMap(v => Right(TestDecodeSet(v)))

        val notEmptyArray = ("1", "1", "2", "3", "2").asJson

        inside(testDecodeSetDecoder.decodeJson(notEmptyArray).getOrElse(TestDecodeSet(Set()))) { case TestDecodeSet(value) =>
          value should contain only ("1", "2", "3")
        }
      }

    }
  }

}
