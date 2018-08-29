package io.tabmo.json.rules

import io.circe.Decoder.Result
import io.circe.{ACursor, Decoder, KeyDecoder}

import scala.collection.generic.CanBuildFrom

private[rules] trait RuleExecutor[I, O] { self: Rule[I, O] =>

  def execute(cursor: ACursor)(implicit d: Decoder[I]): Result[O] =
    cursor.as(createCustomDecoder(d))

  def executeOption(cursor: ACursor)(implicit d: Decoder[Option[I]]): Result[Option[O]] = {
    val customDecoderOpt = d.emap {
      case Some(value)  => validate(value).fold(err => Left(err.toError), decodedValue =>  Right(Some(decodedValue)))
      case None         => Right(None)
    }

    cursor.as(customDecoderOpt)
  }

  def executeSeq(cursor: ACursor)(implicit d: Decoder[I]): Result[Seq[O]] =
    cursor.as[Seq[O]](Decoder.decodeSeq(createCustomDecoder))

  def executeArray(cursor: ACursor)(implicit d: Decoder[I], cbf: CanBuildFrom[Nothing, O, Array[O]]): Result[Array[O]] =
    cursor.as[Array[O]](Decoder.decodeArray(createCustomDecoder, cbf))

  def executeSet(cursor: ACursor)(implicit d: Decoder[I]): Result[Set[O]] =
    cursor.as[Set[O]](Decoder.decodeSet(createCustomDecoder))

  def executeList(cursor: ACursor)(implicit d: Decoder[I]): Result[List[O]] =
    cursor.as[List[O]](Decoder.decodeList(createCustomDecoder))

  def executeVector(cursor: ACursor)(implicit d: Decoder[I]): Result[Vector[O]] =
    cursor.as[Vector[O]](Decoder.decodeVector(createCustomDecoder))

  def executeTraversable(cursor: ACursor)(implicit d: Decoder[I], cbf: CanBuildFrom[Nothing, O, Traversable[O]]): Result[Traversable[O]] =
    cursor.as[Traversable[O]](Decoder.decodeTraversable(createCustomDecoder, cbf))

  def executeMap(cursor: ACursor)(implicit decodeK: KeyDecoder[I], d: Decoder[O]): Result[Map[I, O]] =
    cursor.as[Map[I, O]](Decoder.decodeMap(decodeK, d))

  private def createCustomDecoder(implicit d: Decoder[I]): Decoder[O] = d.emap(value => validate(value).fold(err => Left(err.toError), Right.apply))

}