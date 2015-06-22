organization := "info.exascale"

name := "NLPAnnotator"

version := "1.0"

scalaVersion := "2.11.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")


// --------------------
// --- Dependencies ---
// --------------------

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "edu.arizona.sista" %% "processors" % "5.3",
  "edu.arizona.sista" %% "processors" % "5.3" classifier "models"
)

// Spark
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.3.1" % "provided"

// Misc
libraryDependencies ++= Seq(
  "org.apache.tika" % "tika-core" % "1.7",
  "org.apache.tika" % "tika-parsers" % "1.7",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe" % "config" % "1.2.1",
  "com.typesafe.play" %% "play-json" % "2.3.8"
)

// Scopt
libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

resolvers += Resolver.sonatypeRepo("public")

assemblyMergeStrategy in assembly := {
  case x if x.contains("java_cup") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
