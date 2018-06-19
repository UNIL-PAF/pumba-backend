package ch.unil.paf.pumba.dataset.importer

import ch.unil.paf.pumba.common.rexec.ChangeStatusCallback
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId, DataSetStatus}
import ch.unil.paf.pumba.dataset.services.DataSetService

import scala.concurrent.ExecutionContext

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class DataSetChangeStatus(dataSetService: DataSetService, dataSetId: DataSetId)(implicit ec: ExecutionContext) extends ChangeStatusCallback {

  override def newStatus(status: DataSetStatus, message: Option[String] = None) = {
    val newDataSet = new DataSet(dataSetId, status, message, massFitResult = None)
    dataSetService.updateDataSet(newDataSet)
  }

}
