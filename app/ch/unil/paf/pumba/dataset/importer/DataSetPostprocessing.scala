package ch.unil.paf.pumba.dataset.importer

import ch.unil.paf.pumba.common.rexec.PostprocessingCallback
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetDone, DataSetId, MassFitResult}
import ch.unil.paf.pumba.dataset.services.DataSetService

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class DataSetPostprocessing(dataSetService: DataSetService, dataSetId: DataSetId)(implicit ec: ExecutionContext) extends PostprocessingCallback{

  def startPostProcessing() = {
    // add the MassFitResult
    val oldDataFuture = dataSetService.findDataSet(dataSetId)

    for {
      oldData <- oldDataFuture
      newDataSet = addMassFitResult(oldData)
      ok <- addProteinsToDb(newDataSet)
    } yield (ok)

  }

  private def addMassFitResult(oldDataSetOption: Option[DataSet]): DataSet = {

    val oldDataSet = oldDataSetOption.get
    val massFitResult = MassFitResult(
      massFitPicturePath = s"${oldDataSet.id.value}/mass_fit_res/mass_fit.png",
      massFitRData = s"${oldDataSet.id.value}/mass_fit_res/mass_fit.RData"
    )

    val message = Some("Data was successfully processed.")
    val newDataSet = oldDataSet.copy(massFitResult = Some(massFitResult), status = DataSetDone, message = message)

    dataSetService.updateDataSet(newDataSet)

    newDataSet
  }

  private def addProteinsToDb(dataSet: DataSet): Future[Boolean] = {
    return Future{true}
  }

}
