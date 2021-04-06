
# circe-validation  
  
![Build Status](https://travis-ci.org/tabmo/circe-validation.svg?branch=master)  
[![Gitter](https://badges.gitter.im/tabmo/circe-validation.svg)](https://gitter.im/tabmo/circe-validation?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)  
  
## Overview  
Circe Validation is a library which provides a rules mechanism for decoding circe Json.  
This library is highly inspired by [jto-validation](https://github.com/jto/validation)  
  
## Getting Started  
  
Circe Validation is currently available 2.1X  
Import Circe Validation library, by adding this dependency in your `build.sbt`

Scala <2.13  
```scala  
"io.tabmo" %% "circe-validation-core" % "0.0.6"
```  
  
  Scala 2.13 version :
  
  ```scala  
  "io.tabmo" %% "circe-validation-core" % "0.1.0"
  ```  
and the following resolver  
  
```scala  
"Tabmo Myget Public" at "https://www.myget.org/F/tabmo-public/maven/"
```  
  
If you require some other functionality, you can pick-and-choose from amongst these modules :  
  
 - `circe-validation-extra-rules` : Rules rules for Integer, String & Date  
 - `circe-validation-extra-rules-joda` : Aditional rules for Joda  
  
## Example  
### Using existing rules  
  
```scala  
  
import io.circe.syntax._  
import io.tabmo.json.rules._  
  
case class Person(firstName: String, lastName: String, age: Int, email: String, dateOfBirth: Date, nickname: Option[String])  
  
val decodePerson: Decoder[Person] = Decoder.instance[Person] { (c: Hcursor) =>  
  for {
    name        <- c.downField("name").read(StringRules.maxLength(32))
    lastName    <- c.downField("lastName").as[String]
    age         <- c.downField("age").read(IntRules.positive())
    email       <- c.downField("email").read(StringRules.email)
    dateOfBirth <- c.downField("dateOfBirth").read(DateRules.date)
    nickname    <- c.downField("nickname").readOpt(StringRules.maxLength(32))
  } yield Person(name, lastName, age, email, dateOfBirth, nickname)
}  
  
val personJson = Json.obj(  
  "name" -> "Kevin".asJson,  
  "lastName" -> "Mg".asJson,  
  "age" -> 24.asJson,  
  "email" -> "email@email.com".asJson,  
  "dateOfBirth" -> "1994-02-16".asJson,
  "nickname" -> Json.Null  
)  
 println(decodePerson.decodeJson(personJson)) //Right(Person(Kevin,Mg,24,email@email.com,Wed Feb 16 00:00:00 CET 1994, None))  
  
```  
### Compose multiple rules  
  
It's possible to compose multiple rules with `|+|` method.  
```scala  
import io.circe.syntax._  
import io.tabmo.json.rules._  
  
case class Person(firstName: String, lastName: String, age: Int)  
  
val decodePerson: Decoder[Person] = Decoder.instance[Person] { (c: Hcursor) =>  
  for {  
    name      <- c.downField("name").read(StringRules.maxLength(32) |+| StringRules.isNotEmpty())  
    lastName  <- c.downField("lastName").as[String]  
    age       <- c.downField("age").read(IntRules.positive())  
  } yield Person(name, lastName, age)  
}  
  
val personJson = Json.obj(  
  "name" -> "Kevin".asJson,  
  "lastName" -> "Mg".asJson,  
  "age" ->  24.asJson  
)  
 println(decodePerson.decodeJson(personJson)) //Right(Person(Kevin,Mg,24))  
```  
### Apply a transformation  
  
You can use a transformation rule  
```scala  
import io.circe.syntax._  
import io.tabmo.json.rules._  
  
case class Person(firstName: String, lastName: String, age: Int)  
  
val decodePerson: Decoder[Person] = Decoder.instance[Person] { (c: HCursor) =>  
  for {  
    name      <- c.downField("name").read(StringRules.maxLength(32) |+| StringRules.toUpperCase())  
    lastName  <- c.downField("lastName").read(StringRules.toUpperCase)  
    age       <- c.downField("age").read(IntRules.positive())  
  } yield Person(name, lastName, age)  
}  
  
val personJson = Json.obj(  
  "name" -> "Kevin".asJson,  
  "lastName" -> "Mg".asJson,  
  "age" ->  24.asJson  
)  
 println(decodePerson.decodeJson(personJson)) //Right(Person(KEVIN,MG,24))  
 ```  
  
### Using rules for list/map/set/array  
```scala  
  
import io.circe.syntax._  
import io.tabmo.json.rules._  
  
case class Person(firstName: String, lastName: String, age: Int, carList: Seq[String])  
  
val decodePerson: Decoder[Person] = Decoder.instance[Person] { (c: Hcursor) =>  
  for {  
    name      <- c.downField("name").read(StringRules.maxLength(32))  
    lastName  <- c.downField("lastName").as[String]  
    age       <- c.downField("age").read(IntRules.positive())  
    cars      <- c.downField("cars").readSeq(StringRules.toUpperCase |+| StringRules.maxLength(32))  
  } yield Person(name, lastName, age, cars)  
}  
  
val personJson = Json.obj(  
  "name" -> "Kevin".asJson,  
  "lastName" -> "Mg".asJson,  
  "age" ->  24.asJson,  
  "cars" -> ("Renault", "Mercedes").asJson  
)  
 println(decodePerson.decodeJson(personJson)) //Right(Person(Kevin,Mg,24,List(RENAULT, MERCEDES)))  
```  
  
### Create a custom rule  
It's possible to create a custom rule easily with the trait `GenericRules`  
```scala  
import cats.data.Validated.Valid  
import io.tabmo.json.rules.{GenericRules, Rule}  
  
object HelloRules extends GenericRules {  
    def replaceHiByEmoji: Rule[String, String] = Rule((str: String) => { Valid(str.replaceAll("Hi", ":wave:")) })  
  
    def sayHello(errorCode: String = "error.say.hello.:rage:"): Rule[String, String] =  
      validateWith[String](errorCode)(_.contains("Hello"))  
 }  
```  
  
## Maintainers  
List of current maintainers :  
  
 - [Rlucas12](https://github.com/Rlucas12) - Lucas Reynes  
 - [kevin-margueritte](https://github.com/kevin-margueritte) - Kévin Margueritte  
  
## Copyright & License  
  
Circe Validation is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this file except in compliance with the License.   
  
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
