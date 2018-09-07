name := "stat-scaler"

version := "0.1"

scalaVersion := "2.12.6"

val versions = Map(
  "akka" -> "2.5.16",
  "akka-http" -> "10.1.4",
  "circe" -> "0.9.3",
  "logback-classic" -> "1.1.3",
  "scala-logging" -> "3.9.0",
  "pureconfig" -> "0.9.1"
)

libraryDependencies += "ch.qos.logback" % "logback-classic" % versions("logback-classic") % Runtime
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % versions("scala-logging")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % versions("circe"))

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % versions("pureconfig")
libraryDependencies += "io.kubernetes" % "client-java" % "2.0.0"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % versions("akka")
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % versions("akka")
libraryDependencies += "com.typesafe.akka" %% "akka-http" % versions("akka-http")

mainClass in assembly := Some("app.Application")
assemblyJarName in assembly := "statistic-scaler.jar"

assemblyMergeStrategy in assembly := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
