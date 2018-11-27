package ch.unil.paf.pumba.dataset.services

import ch.unil.paf.pumba.common.helpers.{DatabaseError, DatabaseException}

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId}
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.BSONDocument
import play.modules.reactivemongo.json._


/**
	* @author Roman Mylonas
	* copyright 2018, Protein Analysis Facility UNIL
	*/

class DataSetService(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends DatabaseError{
	val collectionName = "dataset"

	def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))

	/**
		* create a new DataSet
		* @param dataSet
		* @return
		*/
	def insertDataSet(dataSet: DataSet): Future[WriteResult] = {
		val writeRes = collection(collectionName).flatMap(_.insert(dataSet))
		val errorMessage = "Something went wrong while inserting a new DataSet. "

		checkOrError[WriteResult](writeRes, (res) => (! res.ok), (res) => errorMessage + (res.writeErrors.foldLeft("")((a,b) => a + " ; " + b.errmsg)))
	}

	/**
		* remove all DataSets
		* @return
		*/
	def dropAll(): Future[Boolean] = {
		val dropRes = collection(collectionName).flatMap(_.drop(failIfNotFound = false))

		// throw exception if drop went wrong
		val errorMessage = "Something went wrong while dropping all DataSets."

		checkOrError[Boolean](dropRes, (res) => (! res), (res) => (errorMessage))
	}

	/**
		* update a DataSet
		* @param dataSet
		*/
	def updateDataSet(dataSet: DataSet): Future[UpdateWriteResult] = {
		val selector = BSONDocument("id" -> dataSet.id.value)
		val updateRes = collection(collectionName).flatMap(_.update(selector, dataSet, upsert = false))

		// throw exception if the update went wrong
		val errorMessage = s"Something went wrong while updating DataSet [${dataSet.id.value}]. "

		checkOrError[UpdateWriteResult](updateRes, (res) => (! res.ok), (res) => errorMessage + (res.errmsg.getOrElse("Could not recover error message.")))
	}

	/**
		* find a DataSet
		* @param dataSetId
		*/
	def findDataSet(dataSetId: DataSetId): Future[Option[DataSet]] = {
		val query = BSONDocument("id" -> dataSetId.value)
		val findRes = collection(collectionName).flatMap(_.find(query).one[DataSet])

		// throw exception if the update went wrong
		val errorMessage = s"Could not find DataSet [${dataSetId.value}]."

		checkOrError[Option[DataSet]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
	}

}
