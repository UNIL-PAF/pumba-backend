package ch.unil.paf.pumba.common.rserve

import ch.unil.paf.pumba.dataset.models.DataSetStatus

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
trait ChangeStatusCallback {
 def newStatus(status: DataSetStatus, message: Option[String] = None)
}

