package ch.unil.paf.pumba.dataset.services

import ch.unil.paf.pumba.common.helpers.{DatabaseError, DatabaseException}

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId, Sample}
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson._
import play.modules.reactivemongo.json._
import reactivemongo.api.Cursor
import play.api.libs.json._
import reactivemongo.play.json.BSONFormats.BSONDocumentFormat

import scala.util.{Failure, Success}


/**
	* @author Roman Mylonas
	* copyright 2018, Protein Analysis Facility UNIL
	*/

class DataSetService(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends DatabaseError{
	val collectionName = "dataset"

	def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))


	/**
		* list all datasets
		*/
	def list: Future[List[DataSet]] = {
		collection(collectionName).flatMap(_.find(Json.obj()).cursor[DataSet]().collect[List](-1, Cursor.FailOnError[List[DataSet]]()))
	}

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

	/**
		* return a map with samples and a list of dataSetIds corresponding to the given DataSets
		* @param dataSetIds
		* @return
		*/
	def findSamplesDataSetsMap(dataSetIds: List[DataSetId]): Future[Map[Sample, Seq[DataSetId]]] = {
		// find the objects
		val stringifiedIds = dataSetIds.map(_.value)
		val query = BSONDocument("id" -> BSONDocument("$in" -> stringifiedIds))
		val projection = BSONDocument("sample" -> 1, "id" -> 1)
		val findResFuture: Future[List[BSONDocument]] = collection(collectionName).flatMap(_.find(query, projection).cursor[BSONDocument]().collect[List](-1, Cursor.FailOnError[List[BSONDocument]]()))

		// extract the unique samples
		findResFuture.map({ findRes =>
			val extrRes = findRes.map({ oneRes =>
				val sample = Sample(oneRes.getAs[String]("sample").get)
				val dataSetId = DataSetId(oneRes.getAs[String]("id").get)
				(sample, dataSetId)
			})
			extrRes.foldLeft(Map.empty[Sample, Seq[DataSetId]]){ case (acc,(k,v)) =>
				acc.updated(k, acc.getOrElse(k, Seq.empty[DataSetId]) ++ Seq(v))
			}
		})
	}

	/**
		* Remove all proteins with a given dataSetId
		* @param dataSetId
		* @return
		*/
	def removeDataSet(dataSetId: DataSetId): Future[WriteResult] = {
		val query = BSONDocument("id" -> dataSetId.value)
		collection(collectionName).flatMap(_.remove(query))
	}

}
