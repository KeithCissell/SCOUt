name := "SCOUt-sbtBuild"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.12.2"

val Http4sVersion = "0.15.11a"

libraryDependencies ++= Seq(
  "org.http4s"        %% "http4s-blaze-server"  % Http4sVersion,
  "org.http4s"        %% "http4s-circe"         % Http4sVersion,
  "org.http4s"        %% "http4s-dsl"           % Http4sVersion,
  "org.json4s"        %% "json4s-jackson"       % "3.5.2",
  "ch.qos.logback"    % "logback-classic"       % "1.2.1",
  "org.specs2"        %% "specs2-core"          % "3.9.2" % "test",
  "io.circe"          %% "circe-core"           % "0.10.0-M1",
  "io.circe"          %% "circe-parser"         % "0.10.0-M1"
)
