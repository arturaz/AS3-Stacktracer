import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object AS3StacktracerBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    name := "AS3Stacktracer",
    organization := "com.tinylabproductions",
    version := "1.0.0",
    scalaVersion := "2.9.1"
  )

  lazy val stacktracer = Project(
    id = "AS3Stacktracer",
    base = file("."),
    settings = Project.defaultSettings ++ assemblySettings ++ Seq(
      libraryDependencies := Seq(
        "com.github.scala-incubator.io" %% "scala-io-core" % "0.3.0",
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.3.0",
        "org.streum" %% "configrity-core" % "0.10.0"
      )
    )
  )
}
