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
  * copyright 2018-2023, Protein Analysis Facility UNIL
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
    val query = BSONDocument("proteinId" -> proteinId.value, "dataBaseName" -> dataBaseName.value, "isoformId" -> BSONDocument("$exists" ->  0))
    val findRes: Future[List[ProteinSequence]] = collection(collectionName).flatMap(_.find(query).cursor[ProteinSequence]().collect[List](-1, Cursor.FailOnError[List[ProteinSequence]]()))

    val errorMessage = s"Could not find ProteinSequence [${proteinId.value}]."
    checkOrError[List[ProteinSequence]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))
  }

  def findSequence(protein: ProteinId): Future[ProteinSequence] = {
    val query = BSONDocument("proteinId" -> protein.value)
    val findRes: Future[List[ProteinSequence]] = collection(collectionName).flatMap(_.find(query).cursor[ProteinSequence]().collect[List](-1, Cursor.FailOnError[List[ProteinSequence]]()))

    val errorMessage = s"Could not find ProteinSequence [${protein.value}]."
    val futureRes = checkOrError[List[ProteinSequence]](findRes, (res) => (res.isEmpty), (res) => (errorMessage))

    futureRes.map(_.head)
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
  def getSequenceStrings(term: String, organismName: OrganismName, nrResults: Int = 100): Future[List[ProteinSequenceString]] = {

    def query(field: String) = BSONDocument(field -> BSONDocument("$regex" -> term, "$options" -> "i"), "organismName" -> organismName.value, "isoformId" -> BSONDocument("$exists" ->  0))
    def findSeqs(query: BSONDocument) = collection(collectionName).flatMap(_.find(query).cursor[ProteinSequence]().collect[List](nrResults, Cursor.FailOnError[List[ProteinSequence]]()))

    def findProteinId: Future[List[ProteinSequence]] = findSeqs(query("proteinId"))
    def findEntryName: Future[List[ProteinSequence]] = findSeqs(query("geneName"))
    def findProteinName: Future[List[ProteinSequence]] = findSeqs(query("proteinName"))

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

    /* keep only if they really exist as results */
    def queryProt(ac: String) = BSONDocument("proteinIDs" -> ac)
    val protProj = BSONDocument("_id" -> 1)
    def findProt(query: BSONDocument) = collection("protein").flatMap(_.find(query, protProj).cursor[BSONDocument]().collect[List](nrResults, Cursor.FailOnError[List[BSONDocument]]()))

    val resultsVerif: Future[List[(ProteinSequence, Boolean)]] = results.flatMap{ res =>
      Future.sequence{ res.map{ oneRes =>
        val protRes: Future[List[BSONDocument]] = findProt(queryProt(oneRes.proteinId.value))
        protRes.map{a => (oneRes, a.length > 0)}
      }}
    }

    resultsVerif.map{l =>
      l.filter(_._2).sortBy(_._1.proteinName.value).map { e =>
        ProteinSequenceString(e._1.proteinId, e._1.geneName,
          s"${e._1.proteinId.value} | ${e._1.geneName.getOrElse(GeneName("-")).value} | ${e._1.proteinName.value}")
      }
    }
  }



}
