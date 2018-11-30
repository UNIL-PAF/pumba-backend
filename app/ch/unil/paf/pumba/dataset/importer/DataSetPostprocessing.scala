package ch.unil.paf.pumba.dataset.importer

import java.io.File

import ch.unil.paf.pumba.common.rexec.PostprocessingCallback
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.importer.{ImportProteins, ParseProteinGroups}
import ch.unil.paf.pumba.protein.services.ProteinService
import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class DataSetPostprocessing(
                             dataSetService: DataSetService,
                             dataSetId: DataSetId,
                             proteinService: ProteinService,
                             dataRootPath: String
                           )(implicit ec: ExecutionContext) extends PostprocessingCallback{

  def startPostProcessing(): Future[Int] = {
    // add the MassFitResult
    val oldDataFuture = dataSetService.findDataSet(dataSetId)

    for {
      oldData <- oldDataFuture
      newDataSet = addMassFitResult(oldData)
      ok <- addProteinsToDb(newDataSet)
    } yield (ok)

  }

  private def addMassFitResult(oldDataSetOption: Option[DataSet]): DataSet = {
    Logger.info("add mass fit results to dataset")

    val oldDataSet = oldDataSetOption.get
    val massFitResult = MassFitResult(
      massFitPicturePath = s"${oldDataSet.id.value}/mass_fit_res/mass_fit.png",
      massFitRData = s"${oldDataSet.id.value}/mass_fit_res/mass_fit.RData",
      proteinGroupsPath = s"${oldDataSet.id.value}/mass_fit_res/normalizedProteinGroups.txt",
      massFitCoeffs = ParseMassFit().parseCsvCoeffs(s"${dataRootPath}/${oldDataSet.id.value}/mass_fit_res/mass_fit_coeffs.csv"),
      massFits = ParseMassFit().parseCsvFits(s"${dataRootPath}/${oldDataSet.id.value}/mass_fit_res/mass_fits.csv")
    )

    val message = Some("Add proteins to database.")
    val newDataSet = oldDataSet.copy(massFitResult = Some(massFitResult), status = DataSetRunning, message = message)

    dataSetService.updateDataSet(newDataSet)

    newDataSet
  }

  private def addProteinsToDb(dataSet: DataSet): Future[Int] = {
    Logger.info("add proteins to db")
    val proteins = ParseProteinGroups().parseProteinGroupsTable(proteinGroupsFile = new File(dataRootPath + dataSet.massFitResult.get.proteinGroupsPath), dataSetId)
    val res = ImportProteins().importProteins(proteins, proteinService)
    res
  }

}
