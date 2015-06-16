package info.exascale.NLPAnnotator


import edu.stanford.nlp.time.SUTime
import java.time.Duration

object TimeClass {
  var DATE: String = "<TIMEX:DATE>"
  var TIME: String = "<TIMEX:TIME>"
  var MONTH: String = "<TIMEX:MONTH>"
  var INTERVAL: String = "<TIMEX:INTERVAL>"

  def getTimeClass(temporal: SUTime.Temporal): String = {
    val `type`: SUTime.Duration = temporal.getDuration
    "<TIMEX:" + `type`.toString + ">"
  }
}

