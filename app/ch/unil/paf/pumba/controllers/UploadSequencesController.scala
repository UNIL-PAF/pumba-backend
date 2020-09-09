package ch.unil.paf.pumba.controllers

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.Calendar

import ch.unil.paf.pumba.protein.models.OrganismName
import ch.unil.paf.pumba.protein.services.{ProteinService, SequenceService}
import ch.unil.paf.pumba.sequences.importer.ParseFasta
import ch.unil.paf.pumba.sequences.models.DataBaseName
import ch.unil.paf.pumba.sequences.services.ImportSequences
import javax.inject._
import play.api._
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.modules.reactivemongo._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Upload FASTA sequences data
 */
@Singleton
class UploadSequencesController @Inject()(implicit ec: ExecutionContext,
                                          cc: ControllerComponents,
                                          config: Configuration,
                                          val reactiveMongoApi: ReactiveMongoApi)
  extends AbstractController(cc) with MongoController with ReactiveMongoComponents{

  // services
  val sequenceService = new SequenceService(reactiveMongoApi)

  // root data directory
  val rootDataDir = config.get[String]("upload.dir")

  /**
    * Upload a fasta file and start the pre-processing
    */
  def uploadFastaFile(dataBaseName: String, organismName: String) = Action(parse.multipartFormData) { request =>
    // process the ZIP file
    request.body.file("fastaFile").map { fastaFile =>
      val timeStamp: String = Calendar.getInstance().getTime().getTime.toString
      val uploadDir = new File(rootDataDir + timeStamp + "_fasta")

      // copy file to it's new location
      val localFile = copyFile(fastaFile, uploadDir)

      // parse and insert data
      Logger.info("Upload FASTA: start inserting.")
      val sequenceIt = ParseFasta().parse(localFile, DataBaseName(dataBaseName), OrganismName(organismName))
      val res: Future[Int] = ImportSequences().importSequences(sequenceIt, sequenceService)
      res.map(nr => Logger.info(s"Upload FASTA: finished inserting [$nr] sequences."))
    }

    Ok("started FASTA parsing")

  }


  /**
    * Copy the file into the data directory
    *
    * @param file
    * @param newDir
    * @return
    */
  private def copyFile(file: MultipartFormData.FilePart[TemporaryFile], newDir: File): File = {
    // create a new directory and put the zip file in there
    createDir(newDir)

    val filename = Paths.get(file.filename).getFileName
    val fileDest: Path = Paths.get(newDir + "/" + filename.toString)

    file.ref.moveTo(fileDest, replace = true)

    fileDest.toFile
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
