package ch.unil.paf.pumba.controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.modules.reactivemongo._
import java.util.Calendar
import java.io.File
import java.nio.file.{Path, Paths}

import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.dataset.models.DataSet

/**
 * Upload MaxQuant data
 */
@Singleton
class UploadMaxQuantController @Inject()(cc: ControllerComponents, config: Configuration, val reactiveMongoApi: ReactiveMongoApi)
  extends AbstractController(cc) with MongoController with ReactiveMongoComponents{


  // services
  val dataSetService = new DataSetService(reactiveMongoApi)

  /**
    * upload a zip file and start the pre-processing
    * @return
    */
  def uploadZipFile() = Action(parse.multipartFormData) { request =>
    // create a new id
    val dataSetId: String = Calendar.getInstance().getTime().getTime.toString

    val uploadDir = config.get[String]("upload.dir")

    // create a new directory and put the zip file in there
    val newDir = new File(uploadDir + dataSetId)
    if(! newDir.mkdir()){
      throw new Exception(s"could not create the directory [$newDir]")
    }

    // get the zip file and copy it to it's new location
    request.body.file("zipFile").map { zipFile =>
      val filename = Paths.get(zipFile.filename).getFileName
      val fileDest: Path = Paths.get(newDir + "/" + filename.toString)

      zipFile.ref.moveTo(fileDest, replace = true)

      // inset the status into the database
      val dataSet: DataSet = new DataSet(id = dataSetId, status = "zip file uploaded", proteinGroupsFile = None)
      dataSetService.insertDataSet(dataSet)

    }

    Ok(dataSetId)
  }
  
}
