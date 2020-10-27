organization := "totemcag"
name := "minesweeper"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"

libraryDependencies ++= Seq(
  "eu.timepit" %% "refined"                 % "0.9.17"
)

val CirceVersion = "0.13.0"
val Http4sVersion = "0.21.8"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "jul-to-slf4j" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3"
)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-dsl"          % Http4sVersion,
  "org.http4s" %% "http4s-circe"        % Http4sVersion,
  "io.circe"   %% "circe-generic"       % CirceVersion
)


mainClass in (Compile, run) := Some("carlosfau.minesweeper.Main")
mainClass in assembly := Some("carlosfau.minesweeper.Main")
