package ch.unil.paf.pumba.dataset.services

import scala.concurrent.Future
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import ch.unil.paf.pumba.dataset.models.DataSet

class DataSetService(val reactiveMongoApi: ReactiveMongoApi) {
	val collectionName = "dataset"

	//def collection: JSONCollection = db.collection[JSONCollection](collectionName)

	def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))

	// json formatting
	implicit val formatDataSet = Json.format[DataSet]

	def insertDataSet(dataSet: DataSet){

		collection(collectionName).map(_.insert(dataSet).map(lastError =>
      		println("Mongo LastError: %s".format(lastError))
      	))
	}
}
