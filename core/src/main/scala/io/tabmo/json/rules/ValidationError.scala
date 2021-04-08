package io.tabmo.json.rules


case class ValidationError(message: String, args: Any*) {
  def toError: String =
    (message :: args.map(_.toString).toList).mkString("//")
}

object ValidationError {
  def apply(message: String, args: Any*) =
    new ValidationError(message, args: _*)
}

