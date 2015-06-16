package info.exascale.NLPAnnotator

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util._
import info.exascale.NLPAnnotators
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.time._
import edu.stanford.nlp.util.CoreMap
import javax.annotation.Nullable
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

import scala.collection.mutable.ArrayBuffer

object DateAnnotator {
  /**
   * Annotates sentence with TIMEX3 annotations
   * @param sentence - the sentence to be annotated
   * @param date - date of the document, to resolve relative dates, can be { @code null}.
   * @return sentences annotated with TIMEX3 annotations
   */
  def annotate(sentence: String, @Nullable date: String): List[TimeAnnotation] = {
    val annotation: Annotation = new Annotation(sentence)
    if (date != null) annotation.set(classOf[CoreAnnotations.DocDateAnnotation], date)
    NLPAnnotators.dateAnnotationPipeline.annotate(annotation)
    val timexAnnsAll: List[CoreMap] = annotation.get(classOf[CoreAnnotations.TokenEndAnnotation])

    val annotations: List[TimeAnnotation] = new ArrayBuffer[]()
    import scala.collection.JavaConversions._
    for (cm <- timexAnnsAll) {
      val tokens: List[CoreLabel] = cm.get(classOf[CoreAnnotations.TokensAnnotation])
      annotations.add(new Nothing(tokens.get(0).get(classOf[CoreAnnotations.TokenBeginAnnotation]), tokens.get(tokens.size - 1).get(classOf[CoreAnnotations.TokenEndAnnotation]), cm.get(classOf[Nothing]), cm.get(classOf[Nothing]).getTemporal))
    }
    return annotations
  }

  /**
   * Annotates sentence with TIMEX3 annotations, for relative dates, date is set to TODAY.
   * @param sentence - the sentence to be annotated
   * @return sentences annotated with TIMEX3 annotations
   */
  def annotate(sentence: String): String = {
    val dateFormat: DateFormat = new SimpleDateFormat("YYYY-MM-dd")
    val date: String = dateFormat.format(new String)
    return annotate(sentence, date)
  }

  /**
   * Annotates sentence with TIMEX3 annotations, for relative dates, date is set to TODAY.
   * @param sentence - the sentence to be annotated
   * @return sentences annotated with TIMEX3 annotations
   */
  def annotateInline(sentence: String, date: String): String = {
    val tokens: String = new String(Arrays.asList(sentence.split(" ")))
    val annotations: String = annotate(sentence, date)
    import scala.collection.JavaConversions._
    for (annotation <- annotations) {
      val token: String = TimeClass.getTimeClass(annotation.temporal)
      {
        var i: Int = annotation.startToken + 1
        while (i < annotation.endToken) {
          tokens.set(i, null)
          {
            i += 1;
            i - 1
          }
        }
      }
      tokens.set(annotation.startToken, token)
    }
    tokens.removeAll(Collections.singleton(null))
    return String.join(" ", tokens)
  }

  def main(args: Array[String]) {
    annotate("lala 12 Feb 2014", null)
    // try {
    //   readStreamOfLinesUsingFilesWithTryBlock
    // }
    // catch {
    //   case e: IOException => {
    //     e.printStackTrace
    //   }
    // }
  }


  private def readStreamOfLinesUsingFilesWithTryBlock {
    val path: Path = Paths.get("/home/victor/XI/gh/NLPAnnotator/src/main/python/info/exascale/nlp/dateannotator/thrift", "wiki_test_specific_part.txt")
    println(new Date)

    val lines: Stream[String] = Files.lines(path)
    try {
      lines.forEach(s -> annotate(s, null))
    } finally {
      if (lines != null) lines.close()
    }
  }
  println(new Date)

}
