val name = "checklist"
val version = "0.1"
val scalaVersion = "2.13.1"

val akkaVersion = "2.6.4"
val akkaHttpVersion = "10.1.11"

//scalacOptions += "-Xfatal-warnings"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

  "io.spray" %%  "spray-json" % "1.3.5",

  "com.github.pureconfig" %% "pureconfig-magnolia" % "0.12.2",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)