import sbt._

object Dependencies {

  object Version {
    val kindProjectionV = "0.11.0"
    val scalatestCatsV  = "3.0.5"
    val circeV          = "0.13.0"
    val scalatestV      = "3.1.0"
    val jodaV           = "2.10.5"
    val scalacheckV     = "1.14.3"
  }

  object CompilerPlugin {
    val kindProjection  = "org.typelevel" %% "kind-projector" % Version.kindProjectionV cross CrossVersion.full
  }

  object Library {
    val joda            = "joda-time"         % "joda-time"       % Version.jodaV

    val scalatestCats   = "com.ironcorelabs"  %% "cats-scalatest" % Version.scalatestCatsV  % Test
    val scalatest       = "org.scalatest"     %% "scalatest"      % Version.scalatestV      % Test
    val scalacheck      = "org.scalacheck"    %% "scalacheck"     % Version.scalacheckV     % Test
    val scalacheckPlus  = "org.scalatestplus" %% "scalacheck-1-14" % "3.1.0.1"              % Test

    val circe = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % Version.circeV)
  }

}
