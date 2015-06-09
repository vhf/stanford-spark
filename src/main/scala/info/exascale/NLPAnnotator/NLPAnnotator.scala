package info.exascale.NLPAnnotator

import edu.stanford.nlp.ie.crf.CRFClassifier
import java.util.Properties

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
    val annotatedContent = classifier.classifyWithInlineXML("<lala>Barack Obama</lala>")
    val ret = extractEntities(annotatedContent)
    ret.foreach { println }
  }

  private def extractEntities(content: String): Set[String] = {
    extractSingleType(content, "<PERSON>", "</PERSON>") ++
      extractSingleType(content, "<LOCATION>", "</LOCATION>") ++
      extractSingleType(content, "<ORGANIZATION>", "</ORGANIZATION>")
  }

  private def extractSingleType(content: String, openTag: String, closeTag: String): Set[String] = {
    var entities = Set[String]()

    val fragments = content.split(openTag)
    fragments.slice(1, fragments.length).foreach { fragment =>
      val label = fragment.split(closeTag)(0)
      entities += label
    }

    entities.toSet
  }
}
