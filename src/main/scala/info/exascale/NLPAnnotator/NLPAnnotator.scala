package info.exascale.NLPAnnotator

import edu.stanford.nlp.ie.crf.CRFClassifier
import java.util.Properties
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object NER {

  private val props = new Properties()
  props.put("annotators", "tokenize")
  private val classifier = CRFClassifier.getClassifierNoExceptions(
    "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz")

  /**
   * Runs the Stanford Named Entity Reconizer on the given content,
   * printing a Set with all the entity labels.
   */
  def main(args: Array[String]) = {

    val file = "/home/victor/XI/scala/nlpannotator/input.txt" // Should be some file on your system
    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val data = sc.textFile(file, 2).cache()
    val annotatedText = data.map(toAnnotate => classifier.classifyWithInlineXML(toAnnotate))
    prinln(annotatedText)
    // val ret = extractEntities(annotatedText)
    // ret.foreach { println }
  }

  private def extractEntities(content: org.apache.spark.rdd.RDD[String]): Set[String] = {
    extractSingleType(content, "<PERSON>", "</PERSON>") ++
      extractSingleType(content, "<LOCATION>", "</LOCATION>") ++
      extractSingleType(content, "<ORGANIZATION>", "</ORGANIZATION>")
  }

  private def extractSingleType(content: org.apache.spark.rdd.RDD[String], openTag: String, closeTag: String): Set[String] = {
    var entities = Set[String]()

    val fragments = content.split(openTag)
    fragments.slice(1, fragments.length).foreach { fragment =>
      val label = fragment.split(closeTag)(0)
      entities += label
    }

    entities.toSet
  }
}
