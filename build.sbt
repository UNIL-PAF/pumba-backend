name := """Pumba"""
organization := "ch.unil"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
	guice,
	"org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
	"org.reactivemongo" %% "reactivemongo" % "0.13.0",
	"org.reactivemongo" %% "play2-reactivemongo" % "0.13.0-play26",
	"net.lingala.zip4j" % "zip4j" % "1.3.2",
	specs2 % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "ch.unil.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "ch.unil.binders._"

