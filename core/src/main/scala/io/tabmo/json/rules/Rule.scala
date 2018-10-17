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