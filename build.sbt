val doctusVersion = "1.0.6-SNAPSHOT"
val akkaStreamVersion = "2.5.14"
val akkaVersion = "10.1.3"


lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6"
)

lazy val root = (project in file("."))
 .aggregate(core, swing, scalajs, server)
 
lazy val server = (project in file("saint-server"))
  .settings(
      commonSettings,
      mainClass := Some("net.entelijan.SaintServer"),
      assemblyJarName := "saint.jar",
      libraryDependencies += "com.lihaoyi" %%% "utest" % "0.6.3" % "test",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      testFrameworks += new TestFramework("utest.runner.Framework"),
      )  
  .dependsOn(swing)

lazy val core = (project in file("saint-core")) 
   .settings(
      commonSettings,
      libraryDependencies += "net.entelijan" %%% "doctus-core" % doctusVersion,
      libraryDependencies += "com.lihaoyi" %%% "utest" % "0.6.3" % "test",
      )
      .enablePlugins(ScalaJSPlugin)

lazy val swing = (project in file("saint-swing")) 
   .settings(
      commonSettings,
          fork := true,
          libraryDependencies += "com.lihaoyi" %% "upickle" % "0.6.5",
          libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
          libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
          libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaVersion,
          libraryDependencies += "net.entelijan" %% "doctus-jvm" % doctusVersion,
          libraryDependencies += "com.lihaoyi" %%% "utest" % "0.6.3" % "test",
          libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
          libraryDependencies += "com.typesafe" % "config" % "1.2.1",
          libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
          testFrameworks += new TestFramework("utest.runner.Framework"),
      )
      .dependsOn(core)

lazy val scalajs = (project in file("saint-scalajs")) 
   .settings(
      commonSettings,
          libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.6.5",
          libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
          libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.4",
          libraryDependencies += "org.scala-js" %% "scalajs-library" % "0.6.23",
          libraryDependencies += "com.lihaoyi" %%% "utest" % "0.6.3" % "test",
          libraryDependencies += "net.entelijan" %%% "doctus-scalajs" % doctusVersion
      )
      .dependsOn(core)
      .enablePlugins(ScalaJSPlugin)

      