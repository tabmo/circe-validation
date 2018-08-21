package io.tabmo.circe.extra.rules

import java.util.Date

import io.circe.Decoder.Result
import io.circe.syntax._
import io.circe.{Decoder, HCursor, Json}

object Test extends App {

  import io.tabmo.json.rules._

  case class Person(firstName: String, lastName: String, age: Int, email: String, dateOfBirth: Date)

  val personJson = Json.obj(
    "name" -> "Kevin".asJson,
    "lastName" -> "Mg".asJson,
    "age" -> 24.asJson,
    "email" -> "email@email.com".asJson,
    "dateOfBirth" -> "1994-02-16".asJson
  )

  val personJson2 = Json.obj(
    "name" -> "toto".asJson,
    "lastName" -> "titi".asJson,
    "list" -> ("1" :: "2" :: "3" :: Nil).asJson
  )

  val decodePerson: Decoder[Person] = new Decoder[Person] {
    override def apply(c: HCursor): Result[Person] = {
      for {
        name        <- c.downField("name").read(StringRules.maxLength(32))
        lastName    <- c.downField("lastName").as[String]
        age         <- c.downField("age").read(IntRules.positive())
        email       <- c.downField("email").read(StringRules.email)
        dateOfBirth <- c.downField("dateOfBirth").read(DateRules.date)
      } yield Person(name, lastName, age, email, dateOfBirth)
    }
  }

  println(decodePerson.decodeJson(personJson))
  //println(decodePerson.decodeJson(personJson2))

}
