ThisBuild / scalaVersion := "3.5.1"
ThisBuild / organization := "scallbacks"

lazy val scallbacks= project
  .in(file("."))
  .settings(
    name := "Scallbacks",
    libraryDependencies ++= Seq(
      "com.github.cornerman" %% "sloth" % "0.8.0",
      "com.lihaoyi" %% "upickle" % "3.2.0"
    ),
    Compile/mainClass := Some("scallbacks.ServerSubmitter")
  )
