import sbt._
import Keys._
import sbt.Package.ManifestAttributes
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbtassembly._
import sbtassembly.AssemblyKeys._

object SaintBuild extends Build {

  // Constant values
  object D {

    val version = "0.1.0-SNAPSHOT"

    val scalaVersion = "2.11.7"
    val doctusVersion = "1.0.5-SNAPSHOT"
    val akkaStreamVersion = "2.0.1"

  }

  // Settings
  object S {

    lazy val commonSettings =
      Seq(
        version := D.version,
        scalaVersion := D.scalaVersion,
        organization := "net.entelijan",
        resolvers += "entelijan" at "http://entelijan.net/artifactory/repo",
        EclipseKeys.withSource := true,
        assemblyMergeStrategy in assembly <<= (assemblyMergeStrategy in assembly) { (old) => {
          case PathList("JS_DEPENDENCIES") => MergeStrategy.discard
          case x => old(x)
        }
        })

    lazy val coreSettings =
      commonSettings ++
        Seq(
          jsDependencies += RuntimeDOM,
          libraryDependencies += "net.entelijan" %%% "doctus-core" % D.doctusVersion,
          libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.0" % "test")

    lazy val serverSettings =
      commonSettings ++
        Seq(
          mainClass := Some("net.entelijan.SaintServer"),
          assemblyJarName := "saint.jar",
          libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
          libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",
          testFrameworks += new TestFramework("utest.runner.Framework")
        )

    lazy val swingSettings =
      commonSettings ++
        Seq(
          fork := true,
          libraryDependencies += "com.lihaoyi" %% "upickle" % "0.3.4",
          libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % D.akkaStreamVersion,
          libraryDependencies += "com.typesafe.akka" %% "akka-http-core-experimental" % D.akkaStreamVersion,
          libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % D.akkaStreamVersion,
          libraryDependencies += "net.entelijan" %% "doctus-swing" % D.doctusVersion,
          libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
          libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",
          libraryDependencies += "com.typesafe" % "config" % "1.2.1",
          testFrameworks += new TestFramework("utest.runner.Framework"))

    lazy val scalajsSettings =
      commonSettings ++
        Seq(
          jsDependencies += RuntimeDOM,
          libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.3.4",
          libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.8.0",
          libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.8.0",
          libraryDependencies += "org.scala-js" %% "scalajs-library" % "0.6.5",
          libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
          libraryDependencies += "net.entelijan" %%% "doctus-scalajs" % D.doctusVersion)

  }

  // Project definitions
  lazy val root = Project(
    id = "saint-root",
    base = file("."),
    settings = S.commonSettings)
    .aggregate(core, swing, scalajs, server)

  lazy val server = Project(
    id = "saint-server",
    base = file("saint-server"),
    settings = S.serverSettings)
    .dependsOn(swing)

  lazy val core = Project(
    id = "saint-core",
    base = file("saint-core"),
    settings = S.coreSettings)
    .enablePlugins(ScalaJSPlugin)

  lazy val swing = Project(
    id = "saint-swing",
    base = file("saint-swing"),
    settings = S.swingSettings)
    .dependsOn(core)

  lazy val scalajs = Project(
    id = "saint-scalajs",
    base = file("saint-scalajs"),
    settings = S.scalajsSettings)
    .dependsOn(core)
    .enablePlugins(ScalaJSPlugin)

}

