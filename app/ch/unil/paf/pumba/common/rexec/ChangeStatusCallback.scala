package ch.unil.paf.pumba.common.rexec

import ch.unil.paf.pumba.dataset.models.DataSetStatus

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
trait ChangeStatusCallback {
 def newStatus(status: DataSetStatus, message: Option[String] = None)
}

