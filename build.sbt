name := "sunnymart"

version := "1.0"

scalaVersion := "2.12.7"

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.6.0",
  "org.mockito" %% "mockito-scala-scalatest" % "1.16.37" % Test,
  "org.scalatest" %% "scalatest" % "3.2.2" % Test
)