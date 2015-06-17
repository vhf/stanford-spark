package info.exascale.NLPAnnotator

import edu.stanford.nlp.ie.crf.CRFClassifier
import java.util.Properties
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io.File
import scopt.OptionParser

case class Config(
                   input: File = new File("."),
                   output: File = new File("."),
                   props: Seq[String] = Seq(),
                   classifier: String = ""
                   )

object NER {

  val props = new Properties()
  // sbt "run --input ./input.txt --output ./output.txt --properties tokenize,ssplit --classifier edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz"
  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("NLPAnnotator", "0.x")
      opt[File]('i', "input") required() valueName("<file>") action { (x, c) =>
        c.copy(input = x) } text("input file")
      opt[File]('o', "output") required() valueName("<file>") action { (x, c) =>
        c.copy(output = x) } text("output file")
      opt[Seq[String]]('p', "properties") valueName("<prop1>,<prop2>...") action { (x,c) =>
        c.copy(props = x) } text("properties")
      opt[String]('c', "classifier") action { (x, c) =>
        c.copy(classifier = x) } text("Stanford NLP classifier to use")
      help("help") text("prints this usage text")
    }
    parser.parse(args, Config()) match {
      case Some(config) =>
        val properties = config.props mkString ","
        props.put("annotators", properties)

        val classifier = CRFClassifier.getClassifierNoExceptions(config.classifier)

        val input = config.input.getAbsolutePath()
        println(input)
        val output = config.output.getAbsolutePath()
        println(output)
        val conf = new SparkConf().setAppName("NLPAnnotator")
        val sc = new SparkContext(conf)
        val data = sc.textFile(input, 2).cache()
        val annotatedText = data.map(toAnnotate => classifier.classifyWithInlineXML(toAnnotate))
        println(annotatedText)
      case _ =>
        println("error")
    }
  }
}
