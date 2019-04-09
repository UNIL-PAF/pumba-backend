package ch.unil.paf.pumba.controllers

import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.models.ProteinId
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats._
import ch.unil.paf.pumba.protein.services.ProteinService
import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
@Singleton
class DataSetController @Inject()(implicit ec: ExecutionContext,
                                  cc: ControllerComponents,
                                  config: Configuration,
                                  val reactiveMongoApi: ReactiveMongoApi)
  extends AbstractController(cc) with MongoController with ReactiveMongoComponents {

  // services
  val dataSetService = new DataSetService(reactiveMongoApi)
  val proteinService = new ProteinService(reactiveMongoApi)


  def deleteDataSet(dataSetId: String) = Action.async {

    val dataSet = new DataSetId(dataSetId)

    for {
      dataSetRes <- dataSetService.removeDataSet(dataSet)
      proteinRes <- proteinService.removeProteins(dataSet)

    } yield {
      Ok(Json.toJson((dataSetRes.ok & proteinRes.ok)))
    }

  }

  def listDataSets = Action.async {

    for {
      dataSets <- dataSetService.list
    } yield {
      Ok(Json.toJson(dataSets))
    }
  }


}
