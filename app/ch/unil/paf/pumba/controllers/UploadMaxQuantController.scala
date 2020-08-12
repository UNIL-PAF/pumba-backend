package ch.unil.paf.pumba.controllers

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.Calendar

import akka.actor.{ActorSystem, Props}
import ch.unil.paf.pumba.common.rexec.RexecActor
import ch.unil.paf.pumba.dataset.importer.{DataSetChangeStatus, DataSetPostprocessing, ImportMQData}
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.services.{ProteinService, SequenceService}
import javax.inject._
import play.api._
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.modules.reactivemongo._

import scala.concurrent.ExecutionContext

  /**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
  *
  * Upload MaxQuant data
  */
@Singleton
class UploadMaxQuantController @Inject()(implicit ec: ExecutionContext,
                                         cc: ControllerComponents,
                                         config: Configuration,
                                         val reactiveMongoApi: ReactiveMongoApi)
  extends AbstractController(cc) with MongoController with ReactiveMongoComponents{

  // services
  val dataSetService = new DataSetService(reactiveMongoApi)
  val proteinService = new ProteinService(reactiveMongoApi)
  val sequenceService = new SequenceService(reactiveMongoApi)

  // root data directory
  val rootDataDir = config.get[String]("upload.dir")

  /**
    * Upload a zip file and start the pre-processing
    */
  def uploadZipFile(dataSetName: String, sample: String, colorGroup: Int, organism: String, lowDensityThreshold: Option[Int], ignoreSlices: Option[String]) = Action(parse.multipartFormData) { request =>
    // create a new id
    val dataSetId: DataSetId = new DataSetId(Calendar.getInstance().getTime().getTime.toString)
    val uploadDir = new File(rootDataDir + dataSetId.value)
    val rScriptData = config.get[String]("rscript.data")
    val rScriptBin = config.get[String]("rscript.bin")

    // process the ZIP file
    request.body.file("zipFile").map { zipFile =>
      // copy file to it's new location
      val dataDir = copyZipFile(zipFile, uploadDir)

      // set a creation status in the database
      val dataSet: DataSet = DataSet(id = dataSetId, name = dataSetName, sample = Sample(sample), status = DataSetCreated, message = None, massFitResult = None, dataBaseName = None, colorGroup = colorGroup, organism = organism)
      dataSetService.insertDataSet(dataSet)

      // unzip the file
      ImportMQData().unpackZip(dataDir.toFile, uploadDir, removeZip = false)

      // create results directory
      createDir(new File(uploadDir.toString + "/mass_fit_res"))

      // start Rserve for preprocessing
      startScriptToFitMasses(dataSetId, uploadDir, rScriptData, rScriptBin, lowDensityThreshold, ignoreSlices)
    }

    Ok(dataSetId.value)
  }

  /**
    * Start the R script which computes the fit to the masses
    *
    * @param dataSetId
    * @param uploadDir
    * @param rScriptDir
    * @param rScriptBin
    */
  def startScriptToFitMasses(dataSetId: DataSetId,
                             uploadDir: File,
                             rScriptDir: String,
                             rScriptBin: String,
                             lowDensityThreshold: Option[Int],
                             ignoreSlices: Option[String]) = {
    val actorSystem = ActorSystem()

    val changeCallback = new DataSetChangeStatus(dataSetService, dataSetId)
    val postprocCallback = new DataSetPostprocessing(dataSetService, dataSetId, proteinService, sequenceService, rootDataDir)
    val (stdOutFile, stdErrFile) = createRoutputFiles(uploadDir + "/logs")
    val rexecActor = actorSystem.actorOf(
                        Props(new RexecActor(
                                      changeCallback,
                                      postprocCallback,
                                      rScriptBin,
                                      Some(stdOutFile),
                                      Some(stdErrFile))),
                      "rserve")

    import ch.unil.paf.pumba.common.rexec.RexecActor.StartScript
    val scriptParams: List[String] = List("-i " + uploadDir.toString + "/txt/proteinGroups.txt", "-o " + uploadDir.toString + "/mass_fit_res") ++
      lowDensityThreshold.map("--low-density-threshold " + _.toString) ++
      ignoreSlices.map("--ignore-slice " + _)
    rexecActor ! StartScript(filePath = Paths.get(rScriptDir + "create_mass_fit.R"), parameters = scriptParams)
  }


  /**
    * Copy the ZIP file into the data directory
    *
    * @param zipFile
    * @param newDir
    * @return
    */
  private def copyZipFile(zipFile: MultipartFormData.FilePart[TemporaryFile], newDir: File): Path = {
    // create a new directory and put the zip file in there
    createDir(newDir)

    val filename = Paths.get(zipFile.filename).getFileName
    val fileDest: Path = Paths.get(newDir + "/" + filename.toString)

    zipFile.ref.moveTo(fileDest, replace = true)

    fileDest
  }

  /**
    * create log files for R
    * @param logDir
    * @return
    */
  private def createRoutputFiles(logDir: String): (File, File) = {

    createDir(new File(logDir))

    val stdOutFile = new File(logDir + "/r_stdout.log")
    val stdErrFile = new File(logDir + "/r_stderr.log")
    (stdOutFile, stdErrFile)
  }


  /**
    * create a directory
    * @param newDir
    */
  private def createDir(newDir: File) = {
    if(! newDir.mkdir()){
      throw new Exception(s"could not creates directory [${newDir.getAbsolutePath}]")
    }
  }

}
