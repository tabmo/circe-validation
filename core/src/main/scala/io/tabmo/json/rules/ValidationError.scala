package io.tabmo.json.rules


case class ValidationError(message: String, args: String*) {
  def toError: String =
    (message :: args.toList).mkString("//")
}

object ValidationError {
  def apply(message: String, args: String*) =
    new ValidationError(message, args: _*)
}

