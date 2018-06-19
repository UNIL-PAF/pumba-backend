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
    val rScriptDir = config.get[String]("rscript.dir")

    // process the ZIP file
    request.body.file("zipFile").map { zipFile =>
      // copy file to it's new location
      val dataDir = copyZipFile(zipFile, new File(uploadDir + dataSetId.value))

      // set a creation status in the database
      val dataSet: DataSet = new DataSet(id = dataSetId, status = DataSetCreated, message = None, massFitResult = None)
      dataSetService.insertDataSet(dataSet)

      // start Rserve for preprocessing
      startScriptToFitMasses(dataSetId, dataDir, rScriptDir)
    }

    Ok(dataSetId.value)
  }


  /**
    * Start the R script which computes the fit to the masses
    *
    * @param dataSetId
    * @param dataDir
    */
  def startScriptToFitMasses(dataSetId: DataSetId, dataDir: Path, rScriptDir: String) = {
    val actorSystem = ActorSystem()

    val changeCallback = new DataSetChangeStatus(dataSetService, dataSetId)
    val postprocCallback = new DataSetPostprocessing(dataSetService, dataSetId)
    val rserveActor = actorSystem.actorOf(Props(new RexecActor(changeCallback, postprocCallback)), "rserve")

    import ch.unil.paf.pumba.common.rexec.RexecActor.StartScript
    rserveActor ! StartScript(filePath = Paths.get(rScriptDir + "sayHello.R"), parameters = List())
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

}
