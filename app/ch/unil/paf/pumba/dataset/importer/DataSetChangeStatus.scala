package ch.unil.paf.pumba.dataset.importer

import ch.unil.paf.pumba.common.rexec.ChangeStatusCallback
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId, DataSetStatus}
import ch.unil.paf.pumba.dataset.services.DataSetService

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
class DataSetChangeStatus(dataSetService: DataSetService, dataSetId: DataSetId)(implicit ec: ExecutionContext) extends ChangeStatusCallback {

  override def newStatus(status: DataSetStatus, message: Option[String] = None) = {
    val oldDataSetFuture: Future[Option[DataSet]] = dataSetService.findDataSet(dataSetId)
    oldDataSetFuture.flatMap({ oldDataSetOption =>
      val newDataSet = oldDataSetOption.get.copy(status = status, message = message)
      dataSetService.updateDataSet(newDataSet)
    })
  }

}
