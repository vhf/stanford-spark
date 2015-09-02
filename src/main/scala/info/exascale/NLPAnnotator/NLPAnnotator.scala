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
                     props: Seq[String] = Seq(),
                     partitions: Int = 10,
                     lowercase: Boolean = false
                     )

  case class JSONObject(
                      word: String,
                      start: Int,
                      end: Int,
                      pos: ArrayBuffer[String],
                      ner: String
                      )

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
      opt[Int]('t', "partitions") action { (x, c) =>
        c.copy(partitions = x) } text("number of RDD partitions")
      opt[Unit]("lowercase") action { (_, c) =>
        c.copy(lowercase = true) } text("use lowercase models")
      help("help") text("prints this usage text")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val properties = config.props mkString ", "
        val input = config.input.getAbsolutePath()
        val output = config.output.getAbsolutePath()
        val partitions = config.partitions
        val lowercase = config.lowercase

        val conf = new SparkConf().setAppName("NLPAnnotator")
        val sc = new SparkContext(conf)
        val data = sc.textFile(input, partitions).cache()

        // create a Stanford Object
        lazy val pipeline : StanfordCoreNLP = new StanfordCoreNLP({
          val props : Properties = new Properties
          props.put("annotators", properties)
          if (lowercase) {
            props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-caseless-left3words-distsim.tagger")
            props.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.caseless.ser.gz")
            props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.caseless.distsim.crf.ser.gz edu/stanford/nlp/models/ner/english.muc.7class.caseless.distsim.crf.ser.gz edu/stanford/nlp/models/ner/english.conll.4class.caseless.distsim.crf.ser.gz")
          }
          props
        })

        val annotatedText = data.mapPartitions{ iter =>
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
                //(word, ne, offset)
                Map("word" -> word, "ner" -> ner, "beg" -> beg, "end" -> end, "pos" -> pos)
              })
            })

            var out = ArrayBuffer[JSONObject]()
            var currentNerTag = ""
            var currentExpression = ArrayBuffer[String]()
            var currentPosArray = ArrayBuffer[String]()
            var lastTokenEnd = -1

            for (i <- tokens.indices) {
              val token = tokens(i)
              val word = token("word").toString
              val ner = token("ner").toString
              val pos = token("pos").toString
              val beg = token("beg").toString.toInt
              val end = token("end").toString.toInt

              if (ner == "O") {
                currentNerTag = ""
                currentExpression = ArrayBuffer()
                currentPosArray = ArrayBuffer()
                lastTokenEnd = -1
                out += JSONObject(
                  word,
                  beg,
                  end,
                  ArrayBuffer(pos),
                  ner
                )
              } else {
                if (out.isEmpty) {
                  out += JSONObject(
                    word,
                    beg,
                    end,
                    ArrayBuffer(pos),
                    ner
                  )
                } else if (out.last.ner != ner) {
                  out += JSONObject(
                    word,
                    beg,
                    end,
                    ArrayBuffer(pos),
                    ner
                  )
                } else if (out.last.ner == ner) {
                  out(out.length - 1) = JSONObject(
                    out.last.word + " " + word,
                    out.last.start,
                    end,
                    out.last.pos ++= ArrayBuffer(pos),
                    ner
                  )
                }
              }
            }
            out.sortWith(_.start < _.start)
          }
        }
        annotatedText.map(x => Json(DefaultFormats).write(x)).saveAsTextFile(output)
      case _ =>
        println("error")
    }
  }
}
