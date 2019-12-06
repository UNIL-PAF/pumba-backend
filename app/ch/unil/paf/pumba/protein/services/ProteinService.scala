package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.common.helpers.{DataNotFoundException, DatabaseError}
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId, Sample}
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.models.{Protein, ProteinFactory, ProteinId, ProteinWithDataSet}
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
import reactivemongo.play.json.BSONFormats.BSONDocumentFormat

import scala.util.{Failure, Success}

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
class ProteinService (val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends DatabaseError {
  val collectionName = "protein"

  // we need the dataSetService for 
  val dataSetService = new DataSetService(reactiveMongoApi)

  def collection(name: String): Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection](name))

  /**
    * create a new Protein
    *
    * @param protein
    * @return
    */
  def insertProtein(protein: Protein): Future[WriteResult] = {
    val writeRes = collection(collectionName).flatMap(_.insert(protein))

    checkOrError[WriteResult](writeRes, (res) => (!res.ok), (res) => (res.writeErrors.foldLeft("")((a, b) => a + " ; " + b.errmsg)))
  }

  /**
    * remove all Proteins
    *
    * @return
    */
  def dropAll(): Future[Boolean] = {
    val dropRes = collection(collectionName).flatMap(_.drop(failIfNotFound = false))

    // throw exception if drop went wrong
    val errorMessage = "Something went wrong while dropping all Proteins."

    checkOrError[Boolean](dropRes, (res) => (!res), (res) => (errorMessage))
  }

  /**
    * get a protein from a given DataSet
    *
    * @param dataSetId
    * @param proteinId
    */
  def getProteinsFromDataSet(dataSetId: DataSetId, proteinId: ProteinId): Future[List[Protein]] = {
    val query = BSONDocument("dataSetId" -> dataSetId.value, "proteinIDs" -> proteinId.value)
    val findRes: Future[List[Protein]] = collection(collectionName).flatMap(_.find(query).cursor[Protein]().collect[List](-1, Cursor.FailOnError[List[Protein]]()))

    // throw exception if the update went wrong
    val errorMessage = s"Could not find Protein [${proteinId.value}] in [${dataSetId.value}]."
    val proteins = checkOrError[List[Protein]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))

    // give back an empty list if no protein is found for the given DataSet
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
  }


  /**
    * get all unique samples containing the given protein
    *
    * @param proteinId
    * @return
    */
  def getSamplesFromProtein(proteinId: ProteinId): Future[Map[Sample, Seq[DataSetId]]] = {
    val query = BSONDocument("proteinIDs" -> proteinId.value)
    val projection = BSONDocument("dataSetId" -> 1)
    val findResFuture: Future[List[BSONDocument]] = collection(collectionName).flatMap(_.find(query, projection).cursor[BSONDocument]().collect[List](-1, Cursor.FailOnError[List[BSONDocument]]()))

    val dataSetsFuture: Future[List[DataSetId]] = findResFuture.map({
      _.map({ oneRes =>
        DataSetId(oneRes.getAs[String]("dataSetId").get)
      })
    })

    dataSetsFuture.flatMap(dataSetService.findSamplesDataSetsMap(_))
  }


  /**
    * get all proteins with a given searchId
    *
    * @param proteinId
    * @return
    */
  def getProteins(proteinId: ProteinId): Future[List[Protein]] = {
    val query = BSONDocument("proteinIDs" -> proteinId.value)
    val findRes: Future[List[Protein]] = collection(collectionName).flatMap{
      _.find(query).cursor[Protein]().collect[List](-1, Cursor.FailOnError[List[Protein]]())
    }

    val errorMessage = s"Could not find Protein [${proteinId}]."
    checkOrError[List[Protein]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
  }


  /**
    * Keep only DataSetIds in sampleDataSetMap which are listed in dataSetIds. If dataSetIds are None the list is not filtered.
    *
    * @param sampleDataSetMap
    * @param dataSetIds
    * @return
    */
  def filterSampleDataSetMap(sampleDataSetMap: Future[Map[Sample, Seq[DataSetId]]], dataSetIds: Option[Seq[DataSetId]]): Future[Map[Sample, Seq[DataSetId]]] = {
    if (dataSetIds.isDefined) {
      sampleDataSetMap.map { sdsMap =>
        sdsMap.mapValues { (dataSets) =>
          dataSets.filter(dataSetIds.get.contains(_))
        }
      }
    } else {
      sampleDataSetMap
    }
  }

  /**
    * get a list of proteins with their corresponding dataSet information grouped by samples.
    *
    * @param proteinId
    * @param dataSetIds
    * @return
    */
  def getProteinsBySample(proteinId: ProteinId, dataSetIds: Option[Seq[DataSetId]]): Future[Map[Sample, Seq[ProteinWithDataSet]]] = {
    // get the map of samples with corresponding dataSets
    val fSampleDataSetMap = getSamplesFromProtein(proteinId)

    // filter the dataSets
    val fFltSampleDataSetMap:Future[Map[Sample, Seq[DataSetId]]] = filterSampleDataSetMap(fSampleDataSetMap, dataSetIds)

    // get the actual proteins with their dataSet infos
    val sampleProteinMapFutures: Future[Future[Map[Sample, Seq[ProteinWithDataSet]]]] = fFltSampleDataSetMap.map { sampleDataSetMap =>
      val sampleDataSetIter: Iterable[Future[(Sample, Seq[ProteinWithDataSet])]] = sampleDataSetMap.map { case (sample, dataSets) =>
          val seqFRes:Seq[Future[Seq[ProteinWithDataSet]]] = dataSets.map { dataSet =>
            val fProteins: Future[List[Protein]] = getProteinsFromDataSet(dataSet, proteinId)
            addDataSet(fProteins)
          }
          val fResSeqSeq: Future[Seq[Seq[ProteinWithDataSet]]] = Future.sequence { seqFRes }
          val fRes: Future[Seq[ProteinWithDataSet]] = fResSeqSeq.map(_.flatten)
          fRes.map((sample, _))
      }
      Future.sequence(sampleDataSetIter).map(_.toMap)
    }

    val finalMapFuture = sampleProteinMapFutures.flatten

    // filter samples with no proteins
    finalMapFuture.map{ finalMap =>
      finalMap.filter(_._2.length > 0)
    }
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

  /**
    * Remove all proteins with a given dataSetId
    * @param dataSetId
    * @return
    */
  def removeProteins(dataSetId: DataSetId): Future[WriteResult] = {
    val query = BSONDocument("dataSetId" -> dataSetId.value)
    collection(collectionName).flatMap(_.remove(query))
  }


}
