import sbt._
import Keys._
import sbtassembly._
import AssemblyKeys._

object Build extends Build {
  lazy val root = Project("cc", file(".")) settings(coreSettings : _*)

  lazy val commonSettings: Seq[Setting[_]] = Seq(
    organization := "net.gyeh",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.6",
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature")
  )

  lazy val coreSettings = commonSettings ++ Seq(
    name := "cc",
    parallelExecution in Test := false,
    mainClass in assembly := Some("cc.Application"),
    assemblyJarName in assembly := "cc.jar",
    libraryDependencies :=
      Seq(
        "com.github.scopt" %% "scopt" % "3.3.0",
        "org.mapdb" % "mapdb" % "1.0.8",
        "com.google.guava" % "guava" % "18.0",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
        "org.slf4j" % "slf4j-simple" % "1.7.12"
      )
  )
}

