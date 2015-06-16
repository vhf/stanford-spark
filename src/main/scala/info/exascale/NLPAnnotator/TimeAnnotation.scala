package info.exascale.NLPAnnotator

import edu.stanford.nlp.time.SUTime
import edu.stanford.nlp.time.Timex

class TimeAnnotation {
  var startToken: Int = 0
  var endToken: Int = 0
  var temporal: SUTime.Temporal = null
  private var timex: Timex = null

  def this(startToken: Int, endToken: Int, timex: Timex, temporal: SUTime.Temporal) {
    this()
    this.startToken = startToken
    this.endToken = endToken
    this.temporal = temporal
    this.timex = timex
  }

  def getTimexString: String = {
    timex.toString
  }
}
