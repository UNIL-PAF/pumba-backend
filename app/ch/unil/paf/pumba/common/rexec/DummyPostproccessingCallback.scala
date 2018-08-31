package ch.unil.paf.pumba.common.rexec

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
class DummyPostproccessingCallback extends PostprocessingCallback {
  override def startPostProcessing(): Future[Int] = {
    println("start postprocessing")
    Future{0}
  }
}
