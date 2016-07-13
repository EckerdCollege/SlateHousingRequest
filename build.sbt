enablePlugins(JavaAppPackaging)

name := "SlateHousingRequest"
organization := "edu.eckerd"
version := "0.1.0"
maintainer := "Christopher Davenport <ChristopherDavenport@outlook.com>"
packageSummary := "Transfers Housing Requests from Slate Into Banner"

scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.7"
  val scalaTestV  = "2.2.6"
  val slickV = "3.1.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",
    "com.typesafe.slick" %% "slick" % slickV,
    "com.typesafe.slick" %% "slick-extensions" % "3.1.0",
    "com.typesafe.slick" %% "slick-hikaricp" % slickV,
    "com.typesafe" % "config" % "1.3.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  )
}

unmanagedBase := baseDirectory.value / ".lib"
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"

mainClass in Compile := Some("edu.eckerd.integrations.slate.housing.application.ApplicationMain")

mappings in Universal ++= Seq(
  sourceDirectory.value / "main" / "resources" / "application.conf" -> "conf/application.conf",
  sourceDirectory.value / "main" / "resources" / "logback.xml" -> "conf/logback.xml"
)

rpmVendor := "Eckerd College"
rpmLicense := Some("Apache 2.0")