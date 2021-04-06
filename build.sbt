import Dependencies._
import sbt._

organization in ThisBuild := "io.tabmo"
scalaVersion in ThisBuild := "2.13.1"
version in ThisBuild      := "0.1.1"
name                      := "Circe Validation"

licenses in ThisBuild += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))

lazy val commonSettings = Seq(
  scalacOptions           ++= commonScalacOptions,
  fork in test            := true,
  publishTo in ThisBuild  := Some("Tabmo Public MyGet" at "https://www.myget.org/F/tabmo-public/maven/"),
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials-myget") // See https://www.scala-sbt.org/1.x/docs/Publishing.html#Credentials and use the API keys from MyGet
)

lazy val root = (project in file("."))
  .aggregate(core)
  .aggregate(`extra-rules`)
  .aggregate(`extra-rules-joda`)
  .settings(skip in publish := true)

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(commonLibrairies)
  .settings(moduleName := "circe-validation-core", name := "Circe Validation Core")
  .settings(addCompilerPlugin(CompilerPlugin.kindProjection))
  .settings(libraryDependencies +=  Library.scalatestCats)
  .settings(libraryDependencies ++= Library.circe)

lazy val `extra-rules` = (project in file("extra-rules"))
  .settings(commonSettings)
  .settings(commonLibrairies)
  .settings(moduleName := "circe-validation-extra-rules", name := "Circe Validation Extra Rules")
  .dependsOn(core)

lazy val `extra-rules-joda` = (project in file("extra-rules-joda"))
  .settings(moduleName := "circe-validation-extra-rules-joda", name := "Circe Validation Extra Rules Joda")
  .settings(commonSettings)
  .settings(commonLibrairies)
  .settings(libraryDependencies += Library.joda)
  .dependsOn(core)

lazy val commonLibrairies = Seq (
  libraryDependencies += Library.scalatest,
  libraryDependencies += Library.scalacheck,
  libraryDependencies += Library.scalacheckPlus
)

lazy val commonScalacOptions = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
)
