import AssemblyKeys._

assemblySettings

name := "twisearch_ircbot"

version := "2.1"

scalaVersion := "2.10.0"

mainClass in assembly := Some("TwisearchIrcbot")

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-actors" % _)
