package info.exascale.NLPAnnotator

import java.util.Properties
import java.io.File

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

object NER {

  case class Config(
                     input: File = new File("."),
                     output: File = new File("."),
                     props: Seq[String] = Seq()
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
      help("help") text("prints this usage text")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val properties = config.props mkString ", "

        val input = config.input.getAbsolutePath()
        val output = config.output.getAbsolutePath()

//        val conf = new SparkConf().setAppName("NLPAnnotator")
//        val sc = new SparkContext(conf)
//        val data = sc.textFile(input, 2).cache()

        // create a Stanford Object
        lazy val pipeline : StanfordCoreNLP = new StanfordCoreNLP({
          val props : Properties = new Properties
          props.put("annotators", properties)
          props
        })

//        val annotatedText = data.mapPartitions{ iter =>
//          iter.map { part =>
            // create an empty Annotation just with the given text
            val document : Annotation = new Annotation("Barack Hussein Obama II is the 44th and current President of the United States and the first African-American to hold the office. He is a Democrat. Obama won the 2008 United States presidential election, on November 4, 2008. He was inaugurated on January 20, 2009.\n\nAs president, he slowly ended the wars in Afghanistan and Iraq, with intention to prepare the countries so that they could defend themselves. He also signed the Affordable Care Act (often called \"Obamacare\") which changed many health care laws. He also enacted numerous acts to create public works jobs to help the economy. He became the first president to openly express support for gay marriage, proposed gun control as a result of the Sandy Hook school shooting and has called for improving relations with Cuba.")//part)
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
                Map("word" -> word, "ner" -> ner)

              })
            })
            println(tokens)
            var out = ArrayBuffer[Map[String, String]]()
            var currentNerTag = (-1, "")
            var currentExpression = ""
            for (i <- tokens.indices) {
              val token = tokens(i)
              if (token("ner").toString != currentNerTag._2) {
                if (currentExpression != "" && currentNerTag._2.trim != "O") {
                  out += Map("word" -> currentExpression, "ner" -> currentNerTag._2)
                }
                currentNerTag = (i, token("ner").toString)
                currentExpression = token("word")
              } else {
                currentExpression += " " + token("word")
              }
            }
            println(out)
//          }
//        }

//        annotatedText.flatMap(identity).saveAsTextFile(output)
      case _ =>
        println("error")
    }
  }
}
