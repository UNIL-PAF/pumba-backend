package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.common.helpers.{DataNotFoundException, DatabaseError}
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId, Sample}
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.models.{Protein, ProteinFactory, ProteinId, ProteinWithDataSet}
import ch.unil.paf.pumba.sequences.models.SequenceJsonFormats._
import ch.unil.paf.pumba.sequences.models.{DataBaseName, ProteinSequence}
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.json.ImplicitBSONHandlers._

import scala.util.{Failure, Success}

/**
  * @author Roman Mylonas
  * copyright 2018-2019, Protein Analysis Facility UNIL
  */
class SequenceService (val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends DatabaseError {
  val collectionName = "sequence"

  // we need the dataSetService for
  val dataSetService = new DataSetService(reactiveMongoApi)

  def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))

  /**
    * create a new ProteinSequence
    *
    * @param proteinSequence
    * @return
    */
  def insert(proteinSequence: ProteinSequence): Future[WriteResult] = {
    val writeRes = collection(collectionName).flatMap(_.insert(proteinSequence))

    checkOrError[WriteResult](writeRes, (res) => (!res.ok), (res) => (res.writeErrors.foldLeft("")((a, b) => a + " ; " + b.errmsg)))
  }

  /**
    * remove all ProteinsSequences
    *
    * @return
    */
  def dropAll(): Future[Boolean] = {
    val dropRes = collection(collectionName).flatMap(_.drop(failIfNotFound = false))

    // throw exception if drop went wrong
    val errorMessage = "Something went wrong while dropping all ProteinSequences."

    checkOrError[Boolean](dropRes, (res) => (!res), (res) => (errorMessage))
  }


  /**
    * get proteinSequences with a given proteinId
    *
    * @param proteinId
    * @return
    */
  def getSequences(proteinId: ProteinId, dataBaseName: DataBaseName): Future[List[ProteinSequence]] = {
    val query = BSONDocument("proteinId" -> proteinId.value, "dataBaseName" -> dataBaseName.value)
    val findRes: Future[List[ProteinSequence]] = collection(collectionName).flatMap(_.find(query).cursor[ProteinSequence]().collect[List](-1, Cursor.FailOnError[List[ProteinSequence]]()))

    val errorMessage = s"Could not find ProteinSequence [${proteinId}]."
    checkOrError[List[ProteinSequence]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
  }

}
