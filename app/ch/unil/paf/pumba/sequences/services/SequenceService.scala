package ch.unil.paf.pumba.sequences.services

import ch.unil.paf.pumba.common.helpers.{DataNotFoundException, DatabaseError}
import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId, Sample}
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.models.{GeneName, OrganismName, Protein, ProteinFactory, ProteinId, ProteinOrGene, ProteinWithDataSet}
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.sequences.models.{DataBaseName, ProteinSequence, ProteinSequenceString}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONArray, BSONDocument}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.json.ImplicitBSONHandlers._

import scala.util.{Failure, Success}

/**
  * @author Roman Mylonas
  * copyright 2018-2021, Protein Analysis Facility UNIL
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

    val errorMessage = s"Could not find ProteinSequence [${proteinId.value}]."
    checkOrError[List[ProteinSequence]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
  }


  /**
    * get proteinSequences with a given proteinOrGene and filter the results by isoformId if provided
    *
    * @param proteinOrGene
    * @return
    */
  def getSequences(proteinOrGene: ProteinOrGene, organismName: OrganismName, isoformId: Option[Int]): Future[List[ProteinSequence]] = {
    val query = BSONDocument("$or" -> BSONArray(BSONDocument("proteinId" -> proteinOrGene.value), BSONDocument("geneName" -> proteinOrGene.value)), "organismName" -> organismName.value)
    val findRes: Future[List[ProteinSequence]] = collection(collectionName).flatMap(_.find(query).cursor[ProteinSequence]().collect[List](-1, Cursor.FailOnError[List[ProteinSequence]]()))

    val errorMessage = s"Could not find ProteinSequence [${proteinOrGene.value}] from organism [${organismName.value}]."
    val futureRes = checkOrError[List[ProteinSequence]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))

    futureRes.map{ _.filter{ seq =>
      isoformId.isEmpty || ( seq.isoformId.isDefined && seq.isoformId.get == isoformId.get)
    }}
  }

  /**
    * Sequence strings (Name | Gene | description) containing the given term.
    *
    * @param proteinId
    * @param dataBaseName
    * @return
    */
  def getSequenceStrings(term: String, dataBaseName: DataBaseName, nrResults: Int = 30): Future[List[ProteinSequenceString]] = {

    val proteinIdQuery = BSONDocument("proteinId" -> BSONDocument("$regex" -> term), "dataBaseName" -> dataBaseName.value)
    val entryNameQuery = BSONDocument("geneName" -> BSONDocument("$regex" -> term), "dataBaseName" -> dataBaseName.value)
    val proteinNameQuery = BSONDocument("proteinName" -> BSONDocument("$regex" -> term), "dataBaseName" -> dataBaseName.value)

    def commonFind(query: BSONDocument) = collection(collectionName).flatMap(_.find(query).cursor[ProteinSequence]().collect[List](nrResults, Cursor.FailOnError[List[ProteinSequence]]()))

    def findProteinId: Future[List[ProteinSequence]] = commonFind(proteinIdQuery)
    def findEntryName: Future[List[ProteinSequence]] = commonFind(entryNameQuery)
    def findProteinName: Future[List[ProteinSequence]] = commonFind(proteinNameQuery)

    val results: Future[List[ProteinSequence]] = findProteinId.flatMap(resProteinId => {
      val res2F = if(resProteinId.length < nrResults){
        findEntryName.map(resEntryName => (resProteinId ++ resEntryName))
      }else{
        Future{resProteinId}
      }
      res2F.flatMap( res2 => {
        if(res2.length < nrResults){
          findProteinName.map(resProtName => (res2 ++ resProtName))
        }else{
          Future{res2}
        }
      })
    })

    results.map{l =>
      l.map { e =>
        ProteinSequenceString(e.proteinId,
          s"${e.proteinId.value} | ${e.geneName.getOrElse(GeneName("-")).value} | ${e.proteinName.value}")
      }
    }
  }



}
