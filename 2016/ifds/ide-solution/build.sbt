organization := "de.tud.stg"

version := "0.1-SNAPSHOT"

name := "IDE-Solution"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    "de.opal-project" % "abstract-interpretation-framework_2.11" % "0.8.10",
	"ca.mcgill.sable" % "heros" % "trunk",
	"org.scalactic" %% "scalactic" % "2.2.6",
	"org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

EclipseKeys.withSource := true