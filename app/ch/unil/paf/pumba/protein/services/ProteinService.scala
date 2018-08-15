package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.common.helpers.DatabaseError
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId}
import ch.unil.paf.pumba.protein.models.Protein
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.json.ImplicitBSONHandlers._

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class ProteinService (val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends DatabaseError{
  val collectionName = "protein"

  def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))

  /**
    * create a new Protein
    * @param protein
    * @return
    */
  def insertProtein(protein: Protein): Future[WriteResult] = {
    val writeRes = collection(collectionName).flatMap(_.insert(protein))

    checkOrError[WriteResult](writeRes, (res) => (! res.ok), (res) => (res.writeErrors.foldLeft("")((a,b) => a + " ; " + b.errmsg)))
  }

  /**
    * remove all Proteins
    * @return
    */
  def dropAll(): Future[Boolean] = {
    val dropRes = collection(collectionName).flatMap(_.drop(failIfNotFound = false))

    // throw exception if drop went wrong
    val errorMessage = "Something went wrong while dropping all Proteins."

    checkOrError[Boolean](dropRes, (res) => (! res), (res) => (errorMessage))
  }

  /**
    * find a DataSet
    * @param dataSetId
    */
  def findProteins(dataSetId: DataSetId, proteinAc: String): Future[List[Protein]] = {
    val query = BSONDocument("dataSetId" -> dataSetId.value, "proteinIDs" -> proteinAc)
    val findRes:Future[List[Protein]] = collection(collectionName).flatMap(_.find(query).cursor[Protein]().collect[List](-1, Cursor.FailOnError[List[Protein]]()))

    // throw exception if the update went wrong
    val errorMessage = s"Could not find Protein [${proteinAc}] in [${dataSetId.value}]."

    checkOrError[List[Protein]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
  }


}
