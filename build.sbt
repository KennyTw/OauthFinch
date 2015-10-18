name := "Oauth2"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finagle-oauth2" % "0.1.4",
  "com.github.finagle" %% "finch-core" % "0.8.0",
  "com.github.finagle" %% "finch-json4s" % "0.8.0"
)