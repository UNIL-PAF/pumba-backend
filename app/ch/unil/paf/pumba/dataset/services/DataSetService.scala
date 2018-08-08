package ch.unil.paf.pumba.dataset.services

import ch.unil.paf.pumba.common.helpers.DatabaseException

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId}
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.BSONDocument
import play.modules.reactivemongo.json._
import play.api.Logger

import scala.util.{Failure, Success}

class DataSetService(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {
	val collectionName = "dataset"

	def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))

	/**
		* create a new DataSet
		* @param dataSet
		* @return
		*/
	def insertDataSet(dataSet: DataSet): Future[WriteResult] = {
		val writeRes = collection(collectionName).flatMap(_.insert(dataSet))

		checkOrError[WriteResult](writeRes, (res) => (! res.ok), (res) => (res.writeErrors.foldLeft("")((a,b) => a + " ; " + b.errmsg)))
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
		val errorMessage = s"Something went wrong while updating DataSet [${dataSet.id.value}]."

		checkOrError[UpdateWriteResult](updateRes, (res) => (! res.ok), (res) => (res.errmsg.getOrElse(errorMessage)))
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
		* generic function which checks if the result is valid and otherwise throws a DataBaseExcpetion
		* @param res
		* @param check
		* @param error
		* @tparam A
		*/
	private def checkOrError[A](res: Future[A], check: A => Boolean, error: A => String): Future[A] = {
		res.transform {
			case Success(res) => if(check(res)) Failure(new DatabaseException(error(res))) else Success(res)
			case Failure(t) => {
				Logger.error("An error occured in DataSetService: " + t.toString)
				Failure(new DatabaseException(t.getMessage))
			}
		}
	}

}
