package ch.unil.paf.pumba.dataset.importer

import ch.unil.paf.pumba.common.rexec.PostprocessingCallback
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetDone, DataSetId, MassFitResult}
import ch.unil.paf.pumba.dataset.services.DataSetService

import scala.concurrent.ExecutionContext

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class DataSetPostprocessing(dataSetService: DataSetService, dataSetId: DataSetId)(implicit ec: ExecutionContext) extends PostprocessingCallback{

  def startPostProcessing() = {
    // add the MassFitResult
    val oldData = dataSetService.findDataSet(dataSetId)

    oldData.map( addMassFitResult(_) )
  }

  private def addMassFitResult(oldDataSetOption: Option[DataSet]) = {
    val oldDataSet = oldDataSetOption.get
    val massFitResult = new MassFitResult(massFitPicturePath = s"${oldDataSet.id.value}/mass_fit_res/mass_fit.png")
    val message = Some("Data was successfully processed.")
    val newDataSet = oldDataSet.copy(massFitResult = Some(massFitResult), status = DataSetDone, message = message)
    dataSetService.updateDataSet(newDataSet)
  }

}
