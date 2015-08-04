import AssemblyKeys._

assemblySettings

name := "twisearch_ircbot"

version := "3.1"

scalaVersion := "2.11.0"

mainClass in assembly := Some("TwisearchIrcbot")

libraryDependencies ++= List(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12"
)
