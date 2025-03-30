ThisBuild / scalaVersion := "3.5.1"
ThisBuild / organization := "scallbacks"

lazy val hello = project
  .in(file("."))
  .settings(
    name := "Scallbacks",
    libraryDependencies ++= Seq(
      "com.github.cornerman" %% "sloth" % "0.8.0",
      "com.lihaoyi" %% "upickle" % "3.2.0"
    )
  )
