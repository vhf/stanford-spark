package info.exascale.NLPAnnotator

import java.util.Properties
import java.io.File

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

import scala.collection.mutable.ArrayBuffer
import scala.io.Source._

import org.json4s.native.Json
import org.json4s.DefaultFormats


object NER {

  case class Config(
                     input: File = new File("."),
                     output: File = new File("."),
                     props: Seq[String] = Seq()
                     )

  case class JSONObject(
                      word: String,
                      start: Int,
                      end: Int,
                      pos: ArrayBuffer[String],
                      ner: String
                      )

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  val props = new Properties()
  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("NLPAnnotator", "0.x")
      opt[File]('i', "input") required() valueName("<file>") action { (x, c) =>
        c.copy(input = x) } text("input file")
      opt[File]('o', "output") required() valueName("<file>") action { (x, c) =>
        c.copy(output = x) } text("output directory")
      opt[Seq[String]]('p', "properties") valueName("<prop1>,<prop2>...") action { (x,c) =>
        c.copy(props = x) } text("properties")
      help("help") text("prints this usage text")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val properties = config.props mkString ", "

        val input = config.input.getAbsolutePath()
        val output = config.output.getAbsolutePath()

        val conf = new SparkConf().setAppName("NLPAnnotator")
        val sc = new SparkContext(conf)
        val data = sc.textFile(input, 100).cache()

        // create a Stanford Object
        lazy val pipeline : StanfordCoreNLP = new StanfordCoreNLP({
          val props : Properties = new Properties
          props.put("annotators", properties)
          props
        })

        val annotatedText = time {
          data.mapPartitions{ iter =>
            iter.map { part =>
              // create an empty Annotation just with the given text
              val document : Annotation = new Annotation(part)
              pipeline.annotate(document)

              val sentences = document.get(classOf[SentencesAnnotation]).asScala
              val tokens = sentences.toSeq.flatMap(sentence => {
                sentence.get(classOf[TokensAnnotation]).asScala.toSeq.map(token => {
                  val entity = (sentence.get(classOf[TokenBeginAnnotation]), sentence.get(classOf[TokenEndAnnotation]))
                  val word = token.get(classOf[TextAnnotation])
                  val pos = token.get(classOf[PartOfSpeechAnnotation])
                  val ner = token.get(classOf[NamedEntityTagAnnotation])
                  val beg = token.get(classOf[CharacterOffsetBeginAnnotation])
                  val end = token.get(classOf[CharacterOffsetEndAnnotation])
                  Map("word" -> word, "ner" -> ner, "beg" -> beg, "end" -> end, "pos" -> pos)
                })
              })
            }
          }
        }
        annotatedText.saveAsTextFile(output)
      case _ =>
        println("error")
    }
  }
}
