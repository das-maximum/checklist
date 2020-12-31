val name = "checklist"
val version = "0.1"
val scalaVersion = "2.13.4"

val akkaVersion = "2.6.4"
val akkaHttpVersion = "10.1.11"
val circeVersion = "0.13.0"
val enumeratumVersion = "1.6.1"

//scalacOptions += "-Xfatal-warnings"

crossScalaVersions += "2.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,

  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-circe" % enumeratumVersion,

  "com.github.pureconfig" %% "pureconfig" % "0.12.3",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % Test
)
