package ch.unil.paf.pumba.controllers

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.Calendar

import akka.actor.{ActorSystem, Props}
import ch.unil.paf.pumba.common.rexec.RexecActor
import ch.unil.paf.pumba.dataset.importer.{DataSetChangeStatus, DataSetPostprocessing}
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.dataset.services.DataSetService
import javax.inject._
import play.api._
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.modules.reactivemongo._

import scala.concurrent.ExecutionContext

/**
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

  /**
    * Upload a zip file and start the pre-processing
    */
  def uploadZipFile() = Action(parse.multipartFormData) { request =>
    // create a new id
    val dataSetId: DataSetId = new DataSetId(Calendar.getInstance().getTime().getTime.toString)

    val uploadDir = config.get[String]("upload.dir")
    val rScriptData = config.get[String]("rscript.data")
    val rScriptBin = config.get[String]("rscript.bin")

    // process the ZIP file
    request.body.file("zipFile").map { zipFile =>
      // copy file to it's new location
      val dataDir = copyZipFile(zipFile, new File(uploadDir + dataSetId.value))

      // set a creation status in the database
      val dataSet: DataSet = new DataSet(id = dataSetId, status = DataSetCreated, message = None, massFitResult = None)
      dataSetService.insertDataSet(dataSet)

      // start Rserve for preprocessing
      startScriptToFitMasses(dataSetId, dataDir, rScriptData, rScriptBin)
    }

    Ok(dataSetId.value)
  }


  /**
    * Start the R script which computes the fit to the masses
    *
    * @param dataSetId
    * @param dataDir
    */
  def startScriptToFitMasses(dataSetId: DataSetId, dataDir: Path, rScriptDir: String, rScriptBin: String) = {
    val actorSystem = ActorSystem()

    val changeCallback = new DataSetChangeStatus(dataSetService, dataSetId)
    val postprocCallback = new DataSetPostprocessing(dataSetService, dataSetId)
    val (stdOutFile, stdErrFile) = createRoutputFiles(dataDir)
    val rserveActor = actorSystem.actorOf(Props(new RexecActor(changeCallback, postprocCallback, rScriptBin, Some(stdOutFile), Some(stdErrFile))), "rserve")

    import ch.unil.paf.pumba.common.rexec.RexecActor.StartScript
    rserveActor ! StartScript(filePath = Paths.get(rScriptDir + "create_mass_fit.R"), parameters = List())
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
    if(! newDir.mkdir()){
      throw new Exception(s"could not create the directory [$newDir]")
    }

    val filename = Paths.get(zipFile.filename).getFileName
    val fileDest: Path = Paths.get(newDir + "/" + filename.toString)

    zipFile.ref.moveTo(fileDest, replace = true)

    fileDest
  }

  /**
    * create log files for R
    * @param dataDir
    * @return
    */
  private def createRoutputFiles(dataDir: Path): (File, File) = {
    println("dataDir: " + dataDir.toString)
    val stdOutFile = new File(dataDir.toString + "/r_stdout.log")
    val stdErrFile = new File(dataDir.toString + "/r_stderr.log")
    (stdOutFile, stdErrFile)
  }

}
