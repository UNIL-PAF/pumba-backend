package ch.unil.paf.pumba.dataset.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetCreated, DataSetDone, DataSetId}
import ch.unil.paf.pumba.common.helpers.DatabaseException

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures, Waiters}
import play.api.test.Helpers._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import org.scalatest._
import Matchers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class DataSetServiceSpec extends PlayWithMongoSpec with BeforeAndAfter {

  val dataSetService = new DataSetService(reactiveMongoApi)

  before {
    //Init DB
    await {
      val dataSet = DataSet(id = DataSetId("dummy_id"), name = "dummy", cellLine = "Jurkat", status = DataSetCreated, message = None, massFitResult = None)
      dataSetService.insertDataSet(dataSet)
    }
  }

  after {
    //clean DB
    dataSetService.dropAll()
  }

  "DataSetService" should {

    val dataSet = DataSet(id = DataSetId("dummy_id_2"), name = "dummy 2", cellLine = "Jurkat", status = DataSetCreated, message = None, massFitResult = None)

    "insert a DataSet" in {
      val res: WriteResult = await(dataSetService.insertDataSet(dataSet))
      res.ok mustEqual (true)
    }

    "find a DataSet" in {
      val res: Option[DataSet] = await(dataSetService.findDataSet(DataSetId("dummy_id")))
      res.get.id.value mustEqual ("dummy_id")
      res.get.status mustEqual (DataSetCreated)
    }

    "update a DataSet" in {
      val updatedDataset = DataSet(id = DataSetId("dummy_id"), name = "dummy", cellLine = "Jurkat", status = DataSetDone, message = Some("a new message"), massFitResult = None)
      val res: UpdateWriteResult = await(dataSetService.updateDataSet(updatedDataset))
      res.ok mustEqual (true)

      // check if the status changed
      val resFind: Option[DataSet] = await(dataSetService.findDataSet(DataSetId("dummy_id")))
      resFind.get.message.get mustEqual ("a new message")
    }

    "throw exception when not finding" in {
      val res: Future[Option[DataSet]] = dataSetService.findDataSet(DataSetId("not_existing"))

      ScalaFutures.whenReady(res.failed) { e =>
        e shouldBe a [DatabaseException]
      }
    }

  }

}
