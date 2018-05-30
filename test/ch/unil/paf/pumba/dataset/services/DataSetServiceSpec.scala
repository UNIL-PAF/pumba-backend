package ch.unil.paf.pumba.dataset.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetCreated}
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult
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
      val dataSet = DataSet(id = "dummy_id", status = DataSetCreated, proteinGroupsFile = None)
      dataSetService.insertDataSet(dataSet)
    }
  }

  after {
    //clean DB
    dataSetService.dropAll()
  }

  "Insert a DataSet" in {
    val dataSet = DataSet(id = "dummy_id_2", status = DataSetCreated, proteinGroupsFile = None)
    val res:WriteResult = await(dataSetService.insertDataSet(dataSet))
    res.ok mustEqual(true)
  }



}
