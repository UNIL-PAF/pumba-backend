package ch.unil.paf.pumba.dataset.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.common.helpers.{DataNotFoundException, DatabaseException}
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
      val dataSet = DataSet(id = DataSetId("dummy_id"), name = "dummy", sample = Sample("Jurkat"), status = DataSetCreated, message = None, massFitResult = None)
      dataSetService.insertDataSet(dataSet)
      val dataSet_3 = DataSet(id = DataSetId("dummy_id_3"), name = "dummy_3", sample = Sample("Jurkat_3"), status = DataSetCreated, message = None, massFitResult = None)
      dataSetService.insertDataSet(dataSet_3)
    }
  }

  after {
    //clean DB
    dataSetService.dropAll()
  }

  "DataSetService" should {

    val dataSet = DataSet(id = DataSetId("dummy_id_2"), name = "dummy 2", sample = Sample("Jurkat_2"), status = DataSetCreated, message = None, massFitResult = None)

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
      val updatedDataset = DataSet(id = DataSetId("dummy_id"), name = "dummy", sample = Sample("Jurkat"), status = DataSetDone, message = Some("a new message"), massFitResult = None)
      val res: UpdateWriteResult = await(dataSetService.updateDataSet(updatedDataset))
      res.ok mustEqual (true)

      // check if the status changed
      val resFind: Option[DataSet] = await(dataSetService.findDataSet(DataSetId("dummy_id")))
      resFind.get.message.get mustEqual ("a new message")
    }

    "throw exception when not finding" in {
      val res: Future[Option[DataSet]] = dataSetService.findDataSet(DataSetId("not_existing"))

      ScalaFutures.whenReady(res.failed) { e =>
        e shouldBe a [DataNotFoundException]
      }
    }

    "find sample for given DataSets" in {
      val res: List[Sample] = await(dataSetService.findSamplesFromDataSets(List(DataSetId("dummy_id"), DataSetId("dummy_id_3"))))
      res.length mustEqual (2)
      res.contains(Sample("Jurkat_3")) mustEqual true
    }


  }

  "DataSetService with massFitResult" should {

    val massFitRes = MassFitResult("hoho", "hihi", "coucou", Array(3.001,-0.1028208,0.003104945,-3.993684e-05), Array(3.001,-0.1028208,0.003104945,-3.993684e-05), maxInt = 10.9)
    val dataSet2 = DataSet(id = DataSetId("dummy_id_2"), name = "dummy 2", sample = Sample("Jurkat_2"), status = DataSetCreated, message = None, massFitResult = Some(massFitRes))

    "insert a DataSet" in {

      val res2: WriteResult = await(dataSetService.insertDataSet(dataSet2))
      res2.ok mustEqual (true)

    }

  }

}
