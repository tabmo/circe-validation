import sbt._
import Dependencies._

organization in ThisBuild := "io.tabmo"
scalaVersion in ThisBuild := "2.12.4"
version in ThisBuild      := "0.1.1"
name                      := "Circe Validation"
bintrayOrganization       := Some("tabmo")


licenses in ThisBuild += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))

lazy val commonSettings = Seq(
  scalacOptions   ++= commonScalacOptions,
  fork in test    := true
)

lazy val root = (project in file("."))
  .aggregate(core)
  .aggregate(`extra-rules`)
  .aggregate(`extra-rules-joda`)

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(commonLibrairies)
  .settings(addCompilerPlugin(CompilerPlugin.kindProjection))
  .settings(libraryDependencies +=  Library.scalatestCats)
  .settings(libraryDependencies ++= Library.circe)

lazy val `extra-rules` = (project in file("extra-rules"))
  .settings(commonSettings)
  .settings(commonLibrairies)
  .dependsOn(core)

lazy val `extra-rules-joda` = (project in file("extra-rules-joda"))
  .settings(commonSettings)
  .settings(commonLibrairies)
  .settings(libraryDependencies += Library.joda)
  .dependsOn(core)

lazy val commonLibrairies = Seq (
  libraryDependencies += Library.scalatest,
  libraryDependencies += Library.scalacheck
)

lazy val commonScalacOptions = Seq(
  "-deprecation", // Warn when deprecated API are used
  "-feature", // Warn for usages of features that should be importer explicitly
  "-unchecked", // Warn when generated code depends on assumptions
  "-Ywarn-dead-code", // Warn when dead code is identified
  "-Ywarn-numeric-widen", // Warn when numeric are widened
  "-Xlint", // Additional warnings (see scalac -Xlint:help)
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receive
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:existentials",
  "-language:higherKinds",
  "-language:experimental.macros"
)
