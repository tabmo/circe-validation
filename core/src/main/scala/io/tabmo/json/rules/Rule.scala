package io.tabmo.json.rules

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

final case class Rule[I, O](rule: I => Validated[ValidationError, O]) extends RuleExecutor[I, O] {

  import io.tabmo.json.rules.MonadRule._

  def map[OO](f: O => OO): Rule[I ,OO] = monadRule.map(this)(b => f(b))

  def flatMap[OO](f: O => Rule[I, OO]): Rule[I, OO] = monadRule.flatMap(this)(b => f(b))

  def |+|[OO](otherRule: Rule[O, OO]): Rule[I, OO] = compose(otherRule)

  def compose[OO](otherRule: Rule[O, OO]): Rule[I, OO] = {
    val aggregate = rule.andThen {
      case Valid(value)   => otherRule.rule.apply(value)
      case inv@Invalid(_) => inv
    }

    Rule[I, OO](aggregate)
  }

  def validate(value: I): Validated[ValidationError, O] = rule.apply(value)

}

//sealed trait MonadRules[I] extends Monad[Rule[I, ?]] {
//  override def pure[O](x: O): Rule[I, O] = Rule[I, O](_ => Valid(x))
//
//  override def flatMap[O, OO](fa: Rule[I, O])(f: O => Rule[I, OO]): Rule[I, OO] = Rule[I, OO](v => {
//    fa.validate(v).map(f) match {
//      case Valid(r)         => r.rule.apply(v)
//      case inv@Invalid(_)   => inv
//    }
//  })
//
//  override def tailRecM[O, OO](a: O)(f: O => Rule[I, Either[O, OO]]): Rule[I, OO] = Rule[I, OO]( v => {
//    f(a).validate(v) match {
//      case Valid(Right(r))  => Valid(r)
//      case Valid(Left(err)) => tailRecM(err)(f).validate(v)
//      case inv@Invalid(_)   => inv
//    }
//  })
//}