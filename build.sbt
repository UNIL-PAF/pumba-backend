name := """Pumba"""
organization := "ch.unil"

version := "0.7.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.10"

//resolvers ++= Seq(
//	"mvnrepository" at "https://repo.maven.apache.org/maven2"
//)

resolvers += DefaultMavenRepository

libraryDependencies ++= Seq(
	guice,
	"org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
	"org.reactivemongo" %% "reactivemongo" % "0.13.0",
	"org.reactivemongo" %% "play2-reactivemongo" % "0.13.0-play26",
	"net.lingala.zip4j" % "zip4j" % "1.3.2",
	specs2 % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11",
	"org.nuiton.thirdparty" % "REngine" % "1.7-3",
	"org.nuiton.thirdparty" % "Rserve" % "1.7-3",
	"org.biojava" % "biojava-core" % "5.4.0",
	"org.biojava" % "biojava-aa-prop" % "5.4.0",
	"org.apache.commons" % "commons-math3" % "3.6.1"
)

// https://repo.typesafe.com/typesafe/maven-releases/

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "ch.unil.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "ch.unil.binders._"

// run tests with application.test.conf
javaOptions in Test += "-Dconfig.file=conf/application.test.conf"