val name = "checklist"
val version = "0.1"
val scalaVersion = "2.13.6"

val akkaVersion = "2.6.14"
val akkaHttpVersion = "10.2.4"
val circeVersion = "0.13.0"
val enumeratumVersion = "1.6.1"
val tapirVersion = "0.17.19"

scalacOptions += "-Xfatal-warnings"

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.36.0",

  // Tapir
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,

  // Circe
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,

  // Enumeratum
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-circe" % enumeratumVersion,

  // Pure Config
  "com.github.pureconfig" %% "pureconfig" % "0.15.0",

  // Logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  // Test
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.8" % Test
)
