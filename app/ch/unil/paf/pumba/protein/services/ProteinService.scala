package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.common.helpers.{DataNotFoundException, DatabaseError}
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId, Sample}
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.models.{Protein, ProteinFactory, ProteinWithDataSet}
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats._
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
  * copyright 2018, Protein Analysis Facility UNIL
  */
class ProteinService (val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends DatabaseError{
  val collectionName = "protein"

  // we need the dataSetService for 
  val dataSetService = new DataSetService(reactiveMongoApi)

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
    * get a protein from a given DataSet
    * @param dataSetId
    * @param proteinId
    */
  def getProteinsFromDataSet(dataSetId: DataSetId, proteinId: String): Future[List[Protein]] = {
    val query = BSONDocument("dataSetId" -> dataSetId.value, "proteinIDs" -> proteinId)
    val findRes:Future[List[Protein]] = collection(collectionName).flatMap(_.find(query).cursor[Protein]().collect[List](-1, Cursor.FailOnError[List[Protein]]()))

    // throw exception if the update went wrong
    val errorMessage = s"Could not find Protein [${proteinId}] in [${dataSetId.value}]."
    checkOrError[List[Protein]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
  }



  def getSamplesFromProtein(proteinId: String): Future[List[Sample]] = {
    val query = BSONDocument("proteinIDs" -> proteinId)
    val projection = BSONDocument("dataSetId" -> 1)
    val findResFuture: Future[List[BSONDocument]] = collection(collectionName).flatMap(_.find(query, projection).cursor[BSONDocument]().collect[List](-1, Cursor.FailOnError[List[BSONDocument]]()))

    val dataSetsFuture: Future[List[DataSetId]] = findResFuture.map({ _.map({ oneRes =>
      DataSetId(oneRes.getAs[String]("dataSetId").get)
    })})

//    val sampleFuture: Future[List[Sample]] = dataSetsFuture.map { dataSets =>
//      val sampleResFuture: Future[Sample] =
//    }


    dataSetsFuture.onComplete({
      case Success(a) => println("success: " + a)
      case Failure(e) => println("exception: " + e.getMessage)
    })

    Future{List()}
  }


  /**
    * get all proteins with a given searchId
    * @param proteinId
    * @return
    */
  def getProteins(proteinId: String): Future[List[Protein]] = {
    val query = BSONDocument("proteinIDs" -> proteinId)
    val findRes:Future[List[Protein]] = collection(collectionName).flatMap(_.find(query).cursor[Protein]().collect[List](-1, Cursor.FailOnError[List[Protein]]()))

    val errorMessage = s"Could not find Protein [${proteinId}]."
    checkOrError[List[Protein]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
  }


  /**
    * get a list of proteins with their corresponding dataSet information
    * @param proteinId
    * @return
    */
  def getProteinsWithDataSet(proteinId: String): Future[List[ProteinWithDataSet]] = {

    // get the original list of proteins
    val protListFuture: Future[List[Protein]] = getProteins(proteinId)

    addDataSet(protListFuture)
  }


  /**
    * get a list of proteins with their corresponding dataSet information
    * @param proteinId
    * @param dataSetIds
    * @return
    */
  def getProteinsWithDataSet(proteinId: String, dataSetIds: Seq[DataSetId]): Future[List[ProteinWithDataSet]] = {

    // get the original list of proteins
    val protListFuture: Seq[Future[List[Protein]]] = dataSetIds.map( (dataSetId) => {
      val proteins: Future[List[Protein]] = getProteinsFromDataSet(dataSetId, proteinId)

      val transProts: Future[List[Protein]] = proteins.transform({
          case Success(proteins) => Success(proteins)
          case Failure(e) => {
            e match {
              case DataNotFoundException(m, c) => {
                Logger.info(m)
                Success(List.empty)
              }
              case e => Failure(e)
            }
          }
        })
      transProts
    })

    val protFutureList: Future[List[Protein]] = Future.sequence(protListFuture).map(_.flatten.toList)
    addDataSet(protFutureList)
  }

  /**
    * Add dataSet to the selected proteins
    * @param protListFuture
    * @return
    */
  private def addDataSet(protListFuture: Future[List[Protein]]): Future[List[ProteinWithDataSet]] = {
    // add the dataSet information to the proteins
    val newProtListFuture: Future[List[ProteinWithDataSet]] = protListFuture.flatMap( protList =>  {
      val newProtList: List[Future[ProteinWithDataSet]] = protList.map(prot => {
        val dataSetFuture: Future[Option[DataSet]] = dataSetService.findDataSet(prot.dataSetId)
        val newProt: Future[ProteinWithDataSet] = dataSetFuture.map(dataSet => {
          ProteinFactory(prot, dataSet.get)
        })
        newProt
      })

      // convert List[Future] to Future[List]
      Future.sequence(newProtList)
    })

    newProtListFuture
  }

}
