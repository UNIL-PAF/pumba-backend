package ch.unil.paf.pumba.dataset.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetCreated, DataSetDone, DataSetId}
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
class DataSetServiceSpec extends PlayWithMongoSpec with BeforeAndAfter{

  val dataSetService = new DataSetService(reactiveMongoApi)

  before {
    //Init DB
    await {
      val dataSet = DataSet(id = DataSetId("dummy_id"), status = DataSetCreated, message = None)
      dataSetService.insertDataSet(dataSet)
    }
  }

  after {
    //clean DB
    dataSetService.dropAll()
  }

  "DataSetService" should {

    val dataSet = DataSet(id = DataSetId("dummy_id_2"), status = DataSetCreated, message = None)

    "insert a DataSet" in {
      val res:WriteResult = await(dataSetService.insertDataSet(dataSet))
      res.ok mustEqual(true)
    }

    "find a DataSet" in {
      val res: Option[DataSet] = await(dataSetService.findDataSet(DataSetId("dummy_id")))
      res.get.id.value mustEqual("dummy_id")
      res.get.status mustEqual(DataSetCreated)
    }

    "update a DataSet" in {
      val updatedDataset = DataSet(id = DataSetId("dummy_id"), status = DataSetDone, message = Some("a new message"))
      val res: UpdateWriteResult = await(dataSetService.updateDataSet(updatedDataset))
      res.ok mustEqual(true)

      // check if the status changed
      val resFind: Option[DataSet] = await(dataSetService.findDataSet(DataSetId("dummy_id")))
      resFind.get.message.get mustEqual("a new message")
    }



  }



}
