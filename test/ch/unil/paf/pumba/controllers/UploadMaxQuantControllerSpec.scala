package ch.unil.paf.pumba.controllers

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.{Files, Path}
import org.scalatest.BeforeAndAfter
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

import ch.unil.paf.pumba.PlayWithControllerSpec
import ch.unil.paf.pumba.dataset.services.DataSetService

/**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
  */
class UploadMaxQuantControllerSpec extends PlayWithControllerSpec with BeforeAndAfter {

  val dataSetService = new DataSetService(reactiveMongoApi)

  after {
    //clean DB
    dataSetService.dropAll()
  }

  "UploadMaxQuantController POST" should {

    "upload zip" in {

      // we have to create a copy from tiny.zip because the controller deletes the uploaded zip files
      val tempFileCreator = SingletonTemporaryFileCreator
      val tempFile: Path = Files.createTempFile("pumba-tmp-",".zip")
      val origFile: File = new File("test/resources/common/tiny.zip")
      new FileOutputStream(tempFile.toFile) getChannel() transferFrom(
        new FileInputStream(origFile) getChannel, 0, Long.MaxValue )

      // create the multipart form data
      val zipFile = tempFileCreator.create(tempFile)
      val part = FilePart[TemporaryFile](key = "zipFile", filename = "tiny.zip", contentType = Some("application/zip"), ref = zipFile)
      val formData = new MultipartFormData(dataParts = Map(), files = Seq(part), badParts = Seq())

      // make a fake call to the controller
      val controller = app.injector.instanceOf[UploadMaxQuantController]
      val upload = controller.uploadZipFile("dummy", "Jurkat", 1, None, None).apply(FakeRequest(POST, "/upload").withBody(formData))

      // check the results
      status(upload) mustBe OK
      contentType(upload) mustBe Some("text/plain")

      // check that an id (only digits) is returned
      contentAsString(upload).forall(_.isDigit) mustEqual(true)
    }

  }

}
