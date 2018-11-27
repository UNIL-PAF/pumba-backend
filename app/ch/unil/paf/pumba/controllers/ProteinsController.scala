package ch.unil.paf.pumba.controllers
import ch.unil.paf.pumba.common.helpers.DataNotFoundException
import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.services.{ProteinMergeService, ProteinService}
import javax.inject._
import play.api._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.protein.models.ProteinWithDataSet
import play.api.libs.json._

import scala.util.{Failure, Success}

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
@Singleton
class ProteinsController @Inject()(implicit ec: ExecutionContext,
                                   cc: ControllerComponents,
                                   config: Configuration,
                                   val reactiveMongoApi: ReactiveMongoApi)
  extends AbstractController(cc) with MongoController with ReactiveMongoComponents {

  // services
  val proteinService = new ProteinService(reactiveMongoApi)

  /**
    * get the proteins from the backend
    * @param proteinId
    * @return
    */
  def getProteins(proteinId: String) = Action.async {

    val futureProteins: Future[List[ProteinWithDataSet]] = proteinService.getProteinsWithDataSet(proteinId)

    futureProteins.transform({
      case Success(proteins) => Success(Ok(Json.toJson(proteins)))
      case Failure(e) => { e match {
        case DataNotFoundException(m, c) => {
          Logger.error(m)
          Success(BadRequest(m))
        }
        case e => Failure(e)
      }

      }
    })

  }


  def mergeProteins(proteinId: String, dataSetIdString: String) = Action.async{
    val SAMPLE_SEP = ","

    val dataSetIds: Seq[DataSetId] = dataSetIdString.split(SAMPLE_SEP).map(DataSetId(_))

    val proteins = proteinService.getProteinsWithDataSet(proteinId, dataSetIds)

    println(proteins)

    Future{ ??? }
  }


  def multiplyThis(n: Int) = Action.async {
    val rServeHost: String = config.get[String]("Rserve.host")
    val rServePort: Int = config.get[Int]("Rserve.port")

    Future{
      Ok(ProteinMergeService(rServeHost, rServePort).multiplyThis(n).toString)
    }
  }

}
