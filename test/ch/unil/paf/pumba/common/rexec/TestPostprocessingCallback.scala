package ch.unil.paf.pumba.common.rexec

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
class TestPostprocessingCallback extends PostprocessingCallback {
  override def startPostProcessing(): Future[Int] = {
    TestPostprocessingCallback.postProcessingDone = true
    Future{0}
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