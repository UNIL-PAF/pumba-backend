package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.common.helpers.{DataNotFoundException, DatabaseException}
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.protein.models.{Protein, ProteinWithDataSet}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers.await
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test.Helpers._
import org.scalatest._
import Matchers._
import ch.unil.paf.pumba.dataset.services.DataSetService

import scala.concurrent.Future

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
class ProteinServiceSpec extends PlayWithMongoSpec with BeforeAndAfter {

  val proteinService = new ProteinService(reactiveMongoApi)
  val dataSetService = new DataSetService(reactiveMongoApi)

  val protein = Protein(
    dataSetId = DataSetId("dummy_id"),
    proteinIDs = Seq("A0A096LP75", "C4AMC7", "Q6VEQ5", "Q9NQA3", "A8K0Z3"),
    geneNames = Seq("WASH3P", "WASH2P", "WASH6P", "WASH1"),
    theoMolWeight = 50.073,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31049000, 108350000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  )

  val protein_2 = Protein(
    dataSetId = DataSetId("dummy_id"),
    proteinIDs = Seq("A0A096LPI6", "P30042", "A0A096LP75"),
    geneNames = Seq("C21orf33"),
    theoMolWeight = 30.376,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0)
  )

  val protein_3 = Protein(
    dataSetId = DataSetId("dummy_id_2"),
    proteinIDs = Seq("A0A096LPI6", "P30042", "A0A096LP75"),
    geneNames = Seq("C21orf33"),
    theoMolWeight = 30.376,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0)
  )


  val protein_4 = Protein(
    dataSetId = DataSetId("dummy_id_3"),
    proteinIDs = Seq("A0A096LPI6", "P30042", "A0A096LP75"),
    geneNames = Seq("C21orf33"),
    theoMolWeight = 30.376,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0)
  )

  val dataSet_1 = DataSet(
    id = DataSetId("dummy_id"),
    name = "dummy",
    sample = "Jurkat",
    status = DataSetDone,
    message = None,
    massFitResult = None
  )

  val dataSet_2 = DataSet(
    id = DataSetId("dummy_id_2"),
    name = "dummy 2",
    sample = "Jurkat",
    status = DataSetDone,
    message = None,
    massFitResult = None
  )

  val dataSet_3 = DataSet(
    id = DataSetId("dummy_id_3"),
    name = "dummy 3",
    sample = "Blublu",
    status = DataSetDone,
    message = None,
    massFitResult = None
  )

  val dataSet_4 = DataSet(
    id = DataSetId("dummy_id_4"),
    name = "dummy 4",
    sample = "Blublu",
    status = DataSetDone,
    message = None,
    massFitResult = None
  )

  before {
    //Init DB
    await {
      proteinService.insertProtein(protein)
      proteinService.insertProtein(protein_2)
      proteinService.insertProtein(protein_3)
      proteinService.insertProtein(protein_4)
      dataSetService.insertDataSet(dataSet_1)
      dataSetService.insertDataSet(dataSet_2)
      dataSetService.insertDataSet(dataSet_3)
      dataSetService.insertDataSet(dataSet_4)
    }
  }

  after {
    //clean DB
    proteinService.dropAll()
  }

  "ProteinService" should {

    val protein_3 = protein_2.copy(dataSetId = DataSetId("dummy_id_2"))

    "insert a protein" in {
      val res: WriteResult = await(proteinService.insertProtein(protein_3))
      res.ok mustEqual (true)
    }

    "find a protein from a dataset" in {
      val res: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("dummy_id"), "A0A096LPI6"))
      res.length mustEqual 1
      res(0).theoMolWeight mustEqual 30.376
    }

    "find another protein from a dataset" in {
      val res: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("dummy_id"), "C4AMC7"))
      res.length mustEqual 1
      res(0).theoMolWeight mustEqual 50.073
    }

    "find multiple proteins from a dataset" in {
      val res: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("dummy_id"), "A0A096LP75"))
      res.length mustEqual 2
      res.filter(_.theoMolWeight == 30.376).length mustEqual 1
    }

    "throw exception when not finding protein" in {
      val res: Future[List[Protein]] = proteinService.getProteinsFromDataSet(DataSetId("not_existing"), "not_existing")

      ScalaFutures.whenReady(res.failed) { e =>
        e shouldBe a [DataNotFoundException]
      }
    }

    "find protein" in {
      val res: List[Protein] = await(proteinService.getProteins("A0A096LP75"))
      res.length mustEqual 4
      res.filter(_.theoMolWeight == 30.376).length mustEqual 3
    }

    "find protein with dataSet" in {
      val res: List[ProteinWithDataSet] = await(proteinService.getProteinsWithDataSet("A0A096LP75"))
      res.length mustEqual 4
      res(0).dataSet.sample mustEqual("Jurkat")
    }

    "find protein with dataSet for certain dataSets" in {
      val dataSets = Seq(DataSetId("dummy_id"), DataSetId("dummy_id_3"), DataSetId("dummy_id_4"))
      val res: List[ProteinWithDataSet] = await(proteinService.getProteinsWithDataSet("A0A096LP75", dataSetIds = dataSets))

      res.length mustEqual 3
      res(0).dataSet.sample mustEqual("Jurkat")
    }

  }

}
