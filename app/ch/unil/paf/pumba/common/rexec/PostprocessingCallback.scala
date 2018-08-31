package ch.unil.paf.pumba.common.rexec

import scala.concurrent.Future

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
trait PostprocessingCallback {
  def startPostProcessing(): Future[Int]
}
