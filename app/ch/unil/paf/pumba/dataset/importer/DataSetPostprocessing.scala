package ch.unil.paf.pumba.dataset.importer

import java.io.File

import ch.unil.paf.pumba.common.rexec.PostprocessingCallback
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.importer.{ParseParameters, ParsePeptides, ParseProteinGroups}
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.protein.services.{ImportProteins, ProteinService, SequenceService}
import ch.unil.paf.pumba.sequences.importer.ParseFasta
import ch.unil.paf.pumba.sequences.models.DataBaseName
import ch.unil.paf.pumba.sequences.services.ImportSequences
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
                             sequenceService: SequenceService,
                             dataRootPath: String
                           )(implicit ec: ExecutionContext) extends PostprocessingCallback{

  def startPostProcessing(): Future[Int] = {
    // add the MassFitResult
    val oldDataFuture = dataSetService.findDataSet(dataSetId)

    for {
      oldData <- oldDataFuture
      newDataSet = addMassFitResult(oldData)
      ok_prots <- addProteinsToDb(newDataSet)
    } yield (ok_prots)

  }

  private def addMassFitResult(oldDataSetOption: Option[DataSet]): DataSet = {
    Logger.info("add mass fit results to dataset")

    val oldDataSet = oldDataSetOption.get
    val massFitResult = MassFitResult(
      massFitPicturePath = s"${oldDataSet.id.value}/mass_fit_res/mass_fit.png",
      massFitRData = s"${oldDataSet.id.value}/mass_fit_res/mass_fit.RData",
      proteinGroupsPath = s"${oldDataSet.id.value}/mass_fit_res/normalizedProteinGroups.txt",
      peptidesPath = s"${oldDataSet.id.value}/txt/peptides.txt",
      massFitCoeffs = ParseMassFit().parseCsvCoeffs(s"${dataRootPath}/${oldDataSet.id.value}/mass_fit_res/mass_fit_coeffs.csv"),
      massFits = ParseMassFit().parseCsvFits(s"${dataRootPath}/${oldDataSet.id.value}/mass_fit_res/mass_fits.csv"),
      maxInt = ParseMassFit().parseMaxInt(s"${dataRootPath}/${oldDataSet.id.value}/mass_fit_res/max_norm_intensity.csv")
    )

    val dataBaseName: DataBaseName = ParseParameters().parseTable(new File(s"${dataRootPath}/${oldDataSet.id.value}/txt/parameters.txt"))

    val message = Some("Add proteins to database.")
    val newDataSet = oldDataSet.copy(massFitResult = Some(massFitResult), status = DataSetRunning, message = message, dataBaseName = Some(dataBaseName))

    dataSetService.updateDataSet(newDataSet)

    newDataSet
  }

  private def addProteinsToDb(dataSet: DataSet): Future[Int] = {
    Logger.info("add proteins to db")
    val peptides = ParsePeptides().parsePeptidesTable(peptidesFile = new File(dataRootPath + dataSet.massFitResult.get.peptidesPath))
    val proteins = ParseProteinGroups().parseProteinGroupsTable(proteinGroupsFile = new File(dataRootPath + dataSet.massFitResult.get.proteinGroupsPath), dataSetId, peptides)
    val res = ImportProteins().importProteins(proteins, proteinService)
    res
  }

}
