package io.tabmo.json

import io.circe.Decoder.Result
import io.circe.{ACursor, Decoder}

import scala.collection.generic.CanBuildFrom

package object rules {

  import cats.implicits._

  implicit class RichCursor(c: ACursor) {

    def read[I, O](rule: Rule[I, O])(implicit d: Decoder[I]): Result[O] = rule.execute(c)

    def readOpt[I, O](rule: Rule[I, O])(implicit d: Decoder[Option[I]]): Result[Option[O]] = rule.executeOption(c)

    def readSeq[I, O](rule: Rule[I, O])(implicit d: Decoder[I]): Result[Seq[O]] = rule.executeSeq(c)

    def readArray[I, O](rule: Rule[I, O])(implicit d: Decoder[I], cbf: CanBuildFrom[Nothing, O, Array[O]]): Result[Array[O]] = rule.executeArray(c)

    def readSet[I, O](rule: Rule[I, O])(implicit d: Decoder[I]): Result[Set[O]] = rule.executeSet(c)

    def readList[I, O](rule: Rule[I, O])(implicit d: Decoder[I]): Result[List[O]] = rule.executeList(c)

    def readVector[I, O](rule: Rule[I, O])(implicit d: Decoder[I]): Result[Vector[O]] = rule.executeVector(c)

    def readIterable[I, O](rule: Rule[I, O])(implicit d: Decoder[I]): Result[Iterable[O]] = rule.executeIterable(c)

    def readOrElse[I, O](rule: Rule[I, O])(ruleOrElse: Rule[I, O])(implicit d: Decoder[I]): Result[O] = read(rule).orElse(read(ruleOrElse))

  }

}
