package ch.unil.paf.pumba.controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.modules.reactivemongo._
import java.util.Calendar
import java.io.File
import java.nio.file.{Path, Paths}

import akka.actor.{ActorSystem, Props}
import ch.unil.paf.pumba.common.rserve.{DummyChangeStatusCallback, RserveActor}
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.dataset.models._
import play.api.libs.Files.TemporaryFile

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

    // process the ZIP file
    request.body.file("zipFile").map { zipFile =>
      // copy file to it's new location
      val dataDir = copyZipFile(zipFile, new File(uploadDir + dataSetId))

      // set a creation status in the database
      val dataSet: DataSet = new DataSet(id = dataSetId, status = DataSetCreated, proteinGroupsFile = None)
      dataSetService.insertDataSet(dataSet)

      // start Rserve for preprocessing
      val rScriptDir = config.get[String]("rscipt.dir")
      startScriptToFitMasses(dataSetId, dataDir)
    }

    Ok(dataSetId.value)
  }


  /**
    * Start the R script which computes the fit to the masses
    *
    * @param dataSetId
    * @param dataDir
    */
  def startScriptToFitMasses(dataSetId: DataSetId, dataDir: Path) = {
    val actorSystem = ActorSystem()

    val dummyCallback = new DummyChangeStatusCallback(dataSetId)
    val rserveActor = actorSystem.actorOf(Props(new RserveActor(dummyCallback)), "rserve")

    import ch.unil.paf.pumba.common.rserve.RserveActor.StartScript
    rserveActor ! StartScript(filePath = Paths.get("dummy.R"), parameters = List())
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
