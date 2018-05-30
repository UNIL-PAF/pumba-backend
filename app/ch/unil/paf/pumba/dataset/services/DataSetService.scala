package ch.unil.paf.pumba.dataset.services

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import ch.unil.paf.pumba.dataset.models.DataSet
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats._
import reactivemongo.api.commands.WriteResult

class DataSetService(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {
	val collectionName = "dataset"

	//def collection: JSONCollection = db.collection[JSONCollection](collectionName)

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
}
