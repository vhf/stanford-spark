package info.exascale.NLPAnnotators

import edu.stanford.nlp.dcoref.Constants
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.time.TimeAnnotator
import java.io.IOException
import java.util.Properties

object NLPAnnotators {
  var POS_ANNOTATOR: Nothing = new Nothing(false)
  var TIME_ANNOTATOR: TimeAnnotator = null
  var WHITESPACE_TOKENIZER: Nothing = null
  var W2S_ANNOTATOR: Nothing = new Nothing(false)
  var dateAnnotationPipeline: Nothing = null
  var corefPipeline: Nothing = null

  var props: Properties = new Properties
  TIME_ANNOTATOR = new TimeAnnotator("sutime", props)
  WHITESPACE_TOKENIZER = new Nothing(false, TokenizerAnnotator.TokenizerType.Whitespace)
  props = new Properties
  props.setProperty(Constants.DEMONYM_PROP, DefaultPaths.DEFAULT_DCOREF_DEMONYM)
  props.setProperty(Constants.ANIMATE_PROP, DefaultPaths.DEFAULT_DCOREF_ANIMATE)
  props.setProperty(Constants.INANIMATE_PROP, DefaultPaths.DEFAULT_DCOREF_INANIMATE)
  corefPipeline = new Nothing(props)
  dateAnnotationPipeline = new Nothing
  dateAnnotationPipeline.addAnnotator(WHITESPACE_TOKENIZER)
  dateAnnotationPipeline.addAnnotator(W2S_ANNOTATOR)
  dateAnnotationPipeline.addAnnotator(POS_ANNOTATOR)
  dateAnnotationPipeline.addAnnotator(TIME_ANNOTATOR)
}

