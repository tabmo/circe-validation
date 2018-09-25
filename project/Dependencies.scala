import sbt._

object Dependencies {

  object Version {
    val kindProjectionV = "0.9.7"
    val scalatestCatsV  = "2.3.1"
    val circeV          = "0.10.0"
    val scalatestV      = "3.0.5"
    val jodaV           = "2.10"
    val scalacheckV     = "1.13.4"
  }

  object CompilerPlugin {
    val kindProjection  = "org.spire-math"    %% "kind-projector" % Version.kindProjectionV
  }

  object Library {
    val joda            = "joda-time"         % "joda-time"       % Version.jodaV

    val scalatestCats   = "com.ironcorelabs"  %% "cats-scalatest" % Version.scalatestCatsV  % Test
    val scalatest       = "org.scalatest"     %% "scalatest"      % Version.scalatestV      % Test
    val scalacheck      = "org.scalacheck"    %% "scalacheck"     % Version.scalacheckV     % Test

    val circe = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % Version.circeV)
  }

}
