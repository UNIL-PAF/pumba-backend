package ch.unil.paf.pumba.dataset.importer

import ch.unil.paf.pumba.common.rserve.ChangeStatusCallback
import ch.unil.paf.pumba.dataset.models.DataSetStatus
import ch.unil.paf.pumba.dataset.services.DataSetService

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class DataSetChangeStatus(dataSetService: DataSetService) extends ChangeStatusCallback {

  override def newStatus(status: DataSetStatus, message: Option[String] = None) = {

  }

}
