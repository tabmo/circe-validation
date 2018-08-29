package io.tabmo.json.rules

import cats.Monad
import cats.data.Validated.{Invalid, Valid}

object MonadRule {

  implicit def monadRule[I, ?]: Monad[Rule[I, ?]] = new Monad[Rule[I, ?]] {
    override def pure[O](x: O): Rule[I, O] = Rule[I, O](_ => Valid(x))

    override def flatMap[O, OO](fa: Rule[I, O])(f: O => Rule[I, OO]): Rule[I, OO] = Rule[I, OO](v => {
      fa.validate(v).map(f) match {
        case Valid(r)         => r.rule.apply(v)
        case inv@Invalid(_)   => inv
      }
    })

    override def tailRecM[O, OO](a: O)(f: O => Rule[I, Either[O, OO]]): Rule[I, OO] = Rule[I, OO]( v => {
      f(a).validate(v) match {
        case Valid(Right(r))  => Valid(r)
        case Valid(Left(err)) => tailRecM(err)(f).validate(v)
        case inv@Invalid(_)   => inv
      }
    })
  }

}
