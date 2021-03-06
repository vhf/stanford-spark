organization := "info.exascale"

name := "NLPAnnotator"

version := "1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")


// --------------------
// --- Dependencies ---
// --------------------

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

// CoreNLP + resources
libraryDependencies ++= Seq(
    "edu.stanford.nlp" % "stanford-corenlp" % "3.4.1",
    "edu.stanford.nlp" % "stanford-corenlp" % "3.4.1" classifier "models"
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

// json4s
libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.11"


resolvers += Resolver.sonatypeRepo("public")
