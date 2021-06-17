package ch.unil.paf.pumba.controllers

import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.protein.models.{OrganismName, ProteinId}
import ch.unil.paf.pumba.sequences.services.SequenceService
import ch.unil.paf.pumba.sequences.models.{DataBaseName, ProteinSequence, ProteinSequenceString}

import javax.inject._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
  */
@Singleton
class SequenceController @Inject()(implicit ec: ExecutionContext,
                                   cc: ControllerComponents,
                                   config: Configuration,
                                   val reactiveMongoApi: ReactiveMongoApi)
  extends AbstractController(cc) with MongoController with ReactiveMongoComponents {

  // services
  val sequenceService = new SequenceService(reactiveMongoApi)

  /**
    * Get one sequence (should be unique anyway).
    * @param proteinId
    * @return
    */
  def getSequence(proteinId: String, dataBaseName: String) = Action.async {

    val fSeq: Future[List[ProteinSequence]] = sequenceService.getSequences(ProteinId(proteinId), DataBaseName(dataBaseName))

    fSeq.map({ seq =>
      Ok(Json.toJson(seq(0)))
    })

  }

  /**
    * Get multiple sequence strings (Name | Gene | description) containing the given term.
    * First tries to match Name, then Gene, then description and gives back 30 matches.
    * @param term
    * @return
    */
  def getSequenceString(term: String, organismName: String) = Action.async {

    val fSeq: Future[List[ProteinSequenceString]] = sequenceService.getSequenceStrings(term, OrganismName(organismName))

    fSeq.map({ seq =>
      Ok(Json.toJson(seq))
    })

  }

}
