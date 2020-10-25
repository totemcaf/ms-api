name := "mw-api"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"

libraryDependencies ++= Seq(
  "eu.timepit" %% "refined"                 % "0.9.17"

/*
  ,
  "eu.timepit" %% "refined-cats"            % "0.9.17", // optional
  "eu.timepit" %% "refined-eval"            % "0.9.17", // optional, JVM-only
  "eu.timepit" %% "refined-jsonpath"        % "0.9.17", // optional, JVM-only
  "eu.timepit" %% "refined-pureconfig"      % "0.9.17", // optional, JVM-only
  "eu.timepit" %% "refined-scalacheck"      % "0.9.17", // optional
  "eu.timepit" %% "refined-scalaz"          % "0.9.17", // optional
  "eu.timepit" %% "refined-scodec"          % "0.9.17", // optional
  "eu.timepit" %% "refined-scopt"           % "0.9.17", // optional
  "eu.timepit" %% "refined-shapeless"       % "0.9.17"  // optional
 */
)