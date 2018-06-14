package ch.unil.paf.pumba.dataset.services

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId}
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats._
import play.api.libs.json.Json
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.BSONDocument
import play.modules.reactivemongo.json._

class DataSetService(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {
	val collectionName = "dataset"

	def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))

	/**
		* create a new DataSet
		* @param dataSet
		* @return
		*/
	def insertDataSet(dataSet: DataSet): Future[WriteResult] = {
		collection(collectionName).flatMap(_.insert(dataSet))
	}


	/**
		* remove all DataSets
		* @return
		*/
	def dropAll(): Future[Boolean] = {
		collection(collectionName).flatMap(_.drop(failIfNotFound = false))
	}

	/**
		* update a DataSet
		* @param dataSet
		*/
	def updateDataSet(dataSet: DataSet): Future[UpdateWriteResult] = {
		val selector = BSONDocument("id" -> dataSet.id.value)
		collection(collectionName).flatMap(_.update(selector, dataSet, upsert = false))
	}

	/**
		* find a DataSet
		* @param dataSetId
		*/
	def findDataSet(dataSetId: DataSetId): Future[Option[DataSet]] = {
		val query = BSONDocument("id" -> dataSetId.value)
		collection(collectionName).flatMap(_.find(query).one[DataSet])
	}

}
