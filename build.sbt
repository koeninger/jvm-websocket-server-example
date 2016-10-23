scalaVersion := "2.11.4"

val undertowVersion = "2.0.0.Alpha1"

libraryDependencies ++= Seq(
  "io.undertow" % "undertow-core" % undertowVersion,
  "io.undertow" % "undertow-servlet" % undertowVersion,
  "io.undertow" % "undertow-websockets-jsr" % undertowVersion
)
