package ch.unil.paf.pumba.common.rserve

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class TestPostprocessingCallback extends PostprocessingCallback {
  override def startPostProcessing(): Unit = {
    TestPostprocessingCallback.postProcessingDone = true
  }

  /**
    * return info if postprocessing was called and reset it to false
    */
  def isPostprocessingDone() = {
    val isDone = TestPostprocessingCallback.postProcessingDone
    TestPostprocessingCallback.postProcessingDone = false
    isDone
  }

}

object TestPostprocessingCallback {
  var postProcessingDone = false
}