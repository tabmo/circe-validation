package io.tabmo.circe.extra.rules

import io.circe.Decoder.Result
import io.circe.syntax._
import io.circe.{Decoder, HCursor, Json}

object Test extends App {

  import io.tabmo.json.rules._

  case class Person(firstName: String, lastName: String, list: Seq[Int])

  val personJson = Json.obj(
    "name" -> "toto".asJson,
    "lastName" -> "titi".asJson,
    "list" -> ("1" :: "2" :: "3" :: Nil).asJson
  )

  val personJson2 = Json.obj(
    "name" -> "toto".asJson,
    "lastName" -> "titi".asJson,
    "list" -> ("1" :: "2" :: "3" :: Nil).asJson
  )

  val decodePerson: Decoder[Person] = new Decoder[Person] {
    override def apply(c: HCursor): Result[Person] = {
      for {
        name <- c.downField("name").read(StringRules.maxLength(8))
        lastName <- c.downField("lastName").as[String]
        list <- c.downField("list").readSeq(IntRules.max(3) |+| IntRules.max(1))
      } yield Person(name, lastName, list)
    }
  }

  println(decodePerson.decodeJson(personJson))
  //println(decodePerson.decodeJson(personJson2))

}
