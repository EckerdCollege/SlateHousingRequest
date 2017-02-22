enablePlugins(JavaAppPackaging)

name := "SlateHousingRequest"
organization := "edu.eckerd"
version := "0.1.0"
maintainer := "Christopher Davenport <ChristopherDavenport@outlook.com>"
packageSummary := "Transfers Housing Requests from Slate Into Banner"

scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val scalaTestV  = "3.0.0"
  val slickV = "3.1.1"
  Seq(
    "edu.eckerd"        %% "slate-core" % "0.1.0",
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",
    "com.typesafe.slick" %% "slick" % slickV,
    "com.typesafe.slick" %% "slick-extensions" % "3.1.0",
    "com.typesafe.slick" %% "slick-hikaricp" % slickV,
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
    "com.h2database" % "h2" % "1.4.187" % "test",
    "org.typelevel" %% "cats" % "0.6.1"
  )
}

unmanagedBase := baseDirectory.value / ".lib"
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")

mainClass in Compile := Some("edu.eckerd.integrations.slate.housing.application.ApplicationMain")

mappings in Universal ++= Seq(
  sourceDirectory.value / "main" / "resources" / "application.conf" -> "conf/application.conf",
  sourceDirectory.value / "main" / "resources" / "logback.xml" -> "conf/logback.xml"
)
rpmVendor := "Eckerd College"
rpmLicense := Some("Apache 2.0")