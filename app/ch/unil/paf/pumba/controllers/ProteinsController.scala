package ch.unil.paf.pumba.controllers
import ch.unil.paf.pumba.common.helpers.DataNotFoundException
import ch.unil.paf.pumba.dataset.models.{DataSetId, Sample}
import ch.unil.paf.pumba.protein.services.{ProteinMergeService, ProteinService}
import javax.inject._
import play.api._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats.{formatDataSet, formatDataSetId, formatSample}
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.protein.models.{ProteinId, ProteinMerge, ProteinWithDataSet, TheoMergedProtein}
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

  // environment variables
  val rServeHost: String = config.get[String]("Rserve.host")
  val rServePort: Int = config.get[Int]("Rserve.port")

  /**
    * Merge proteins from all datasets and create a theoretical merge.
    * @param proteinId
    * @return
    */
  def mergeProteins(proteinId: String, dataSetString: Option[String]) = Action.async{

    val dataSetIds: Option[Seq[DataSetId]] = dataSetString.map( s => {
      val SAMPLE_SEP = ","
      s.split(SAMPLE_SEP).map(DataSetId(_))
    })

    val sampleProteinsMap: Future[Map[Sample, Seq[ProteinWithDataSet]]] = proteinService.getProteinsBySample(ProteinId(proteinId), dataSetIds)

    val proteinMerges: Future[Seq[ProteinMerge]] = sampleProteinsMap.map {
      _.map { case (sample, protList) =>
        ProteinMergeService(rServeHost, rServePort).mergeProteins(protList, sample)
      }.toSeq
    }

    proteinMerges.map({ merges =>
      Ok(Json.toJson(merges))
    })

  }


}
