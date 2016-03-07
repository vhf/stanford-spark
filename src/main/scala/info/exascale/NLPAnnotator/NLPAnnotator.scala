package info.exascale.NLPAnnotator

import java.util.Properties
import java.io.File

import edu.stanford.nlp.ie.crf.CRFClassifier
import org.apache.spark.rdd.RDD

import scala.collection.JavaConverters._
import collection.JavaConversions._

import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations.EntityMentionsAnnotation
import edu.stanford.nlp.util.CoreMap
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import scopt.OptionParser



object NER {

  case class Config(
                     input: File = new File("."),
                     output: File = new File("."),
                     props: Seq[String] = Seq(),
                     partitions: Int = 40,
                     column: Int = 5
                   )

  val props = new Properties()

  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("NLPAnnotator", "0.x")
      opt[File]('i', "input") required() valueName ("<file>") action { (x, c) =>
        c.copy(input = x)
      } text ("input file")
      opt[File]('o', "output") required() valueName ("<file>") action { (x, c) =>
        c.copy(output = x)
      } text ("output directory")
      opt[Seq[String]]('p', "properties") valueName ("<prop1>,<prop2>...") action { (x, c) =>
        c.copy(props = x)
      } text ("properties")
      opt[Int]('t', "partitions") action { (x, c) =>
        c.copy(partitions = x)
      } text ("number of RDD partitions")
      opt[Int]('c', "column") action { (x, c) =>
        c.copy(column = x)
      } text ("TSV column to run NER on")
      help("help") text ("prints this usage text")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val properties = config.props mkString ", "
        val input = config.input.getAbsolutePath()
        val output = config.output.getAbsolutePath()
        val partitions = config.partitions
        val column = config.column

        val conf = new SparkConf().setAppName("NLPAnnotator")
        val sc = new SparkContext(conf)
        val data = sc.textFile(input, partitions).cache()

        lazy val classifier = CRFClassifier.getClassifier("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz")

        val annotatedText = data.mapPartitions { iter =>
          iter.map { part =>
            return part.split("\n").map { line =>
              val tweet = line.split("\t")(column)
              println(tweet)
              val classified = tweet + "\t" + classifier.classifyWithInlineXML(tweet)
              println(classified)
              return classified
            }
          }
        }
        annotatedText.saveAsTextFile(output)
      case _ =>
        println("error")
    }
  }
}
