package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models.Protein
import ch.unil.paf.pumba.protein.services.ProteinService
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ImportProteinsSpec extends PlayWithMongoSpec with BeforeAndAfter {

  val proteinService = new ProteinService(reactiveMongoApi)

  before {
    //Init DB
  }

  after {
    //clean DB
    proteinService.dropAll()
  }

  "importProteins" should {

    // prepare the proteinIterator
    val dataSetId = DataSetId("dummy_id")
    val proteinGroupsFile = new File("test/resources/max_quant/tiny_proteinGroups.txt")
    val proteins: Iterator[Protein] = ParseProteinGroups().parseProteinGroupsTable(proteinGroupsFile, dataSetId)
    val nrImports = await(ImportProteins().importProteins(proteins, proteinService))


    "import the correct number of proteins" in {
      nrImports mustEqual(99)
    }

    "protein [A0A024R216] should be inserted" in {
      val proteins: List[Protein] = await(proteinService.findProteins(dataSetId, "Q9Y3E1"))
      proteins.length mustEqual(99)
      proteins(0).proteinIDs mustEqual(Seq("A0A024R216"))

    }
  }

}
