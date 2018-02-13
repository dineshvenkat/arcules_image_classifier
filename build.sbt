import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.docker._

name := """ImageClassifier"""

version := "v29"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies += guice
libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "1.16.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"
libraryDependencies += "com.google.cloud.sql"% "mysql-socket-factory" % "1.0.2"
            
dockerBaseImage := "arcules_base"


mappings in Universal ++= (baseDirectory.value / "scripts" * "*" get) map
  (x => x -> ("bin/" + x.getName))
