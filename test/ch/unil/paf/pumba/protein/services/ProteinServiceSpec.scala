package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.common.helpers.DatabaseException
import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models.Protein

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers.await
import reactivemongo.api.commands.WriteResult
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test.Helpers._
import org.scalatest._
import Matchers._
import scala.concurrent.Future

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
class ProteinServiceSpec extends PlayWithMongoSpec with BeforeAndAfter {

  val proteinService = new ProteinService(reactiveMongoApi)

  val protein = Protein(
    dataSetId = DataSetId("dummy_id"),
    proteinIDs = List("A0A096LP75", "C4AMC7", "Q6VEQ5", "Q9NQA3", "A8K0Z3"),
    geneNames = List("WASH3P", "WASH2P", "WASH6P", "WASH1"),
    theoMolWeight = 50.073,
    intensities = List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31049000, 108350000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  )

  val protein_2 = Protein(
    dataSetId = DataSetId("dummy_id"),
    proteinIDs = List("A0A096LPI6", "P30042", "A0A096LP75"),
    geneNames = List("C21orf33"),
    theoMolWeight = 30.376,
    intensities = List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0)
  )

  before {
    //Init DB
    await {
      proteinService.insertProtein(protein)
      proteinService.insertProtein(protein_2)
    }
  }

  after {
    //clean DB
    proteinService.dropAll()
  }

  "ProteinService" should {

    val protein_3 = protein_2.copy(dataSetId = DataSetId("dummy_id_2"))

    "insert second protein" in {
      val res: WriteResult = await(proteinService.insertProtein(protein_3))
      res.ok mustEqual (true)
    }

    "find a protein" in {
      val res: List[Protein] = await(proteinService.findProteins(DataSetId("dummy_id"), "A0A096LPI6"))
      res.length mustEqual 1
      res(0).theoMolWeight mustEqual 30.376
    }

    "find multiple proteins" in {
      val res: List[Protein] = await(proteinService.findProteins(DataSetId("dummy_id"), "A0A096LP75"))
      res.length mustEqual 2
      res.filter(_.theoMolWeight == 30.376).length mustEqual 1
    }

    "throw exception when not finding" in {
      val res: Future[List[Protein]] = proteinService.findProteins(DataSetId("not_existing"), "not_existing")

      ScalaFutures.whenReady(res.failed) { e =>
        e shouldBe a [DatabaseException]
      }
    }

  }

}
