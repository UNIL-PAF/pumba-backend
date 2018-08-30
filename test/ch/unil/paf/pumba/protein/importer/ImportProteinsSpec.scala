package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models.Protein
import org.specs2.mutable.Specification

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ImportProteinsSpec extends Specification{

  val proteinGroupsFile = new File("test/resources/max_quant/tiny_proteinGroups.txt")

  "parseHeaders" should {

    val headerLine:String = Source.fromFile(proteinGroupsFile).getLines().next

    "give back correct HashMap" in {

      val headers: Map[String, Int] = ImportProteins().parseHeaders(headerLine)
      headers.size mustEqual(717)
      headers("majority.protein.ids") mustEqual(1)

    }

    "get correct intensity positions" in {
      val headers: Map[String, Int] = ImportProteins().parseHeaders(headerLine)
      val intPos = ImportProteins().getIntensityPositions(headers, "intensity.h.")

      intPos.length mustEqual(45)
      intPos(0) mustEqual(572)
      intPos.last mustEqual(704)
    }

  }

  "parseProteinGroupsTable" should {

    val proteinsList: Seq[Protein] = ImportProteins().parseProteinGroupsTable(proteinGroupsFile, DataSetId("dummy_id")).toList

    "get correct number of proteins" in {
      proteinsList.length mustEqual(99)
    }

    "get correct protein content" in {
      val protein = proteinsList(0)
      protein.intensities(0) mustEqual(0)
      protein.intensities(34) mustEqual(4377600)
      protein.proteinIDs.length mustEqual(2)
      protein.proteinIDs(1) mustEqual("Q9Y3E1")
      protein.geneNames.length mustEqual(1)
      protein.geneNames(0) mustEqual("HDGFRP3")
      protein.dataSetId.value mustEqual("dummy_id")
      protein.theoMolWeight mustEqual(22.619)
    }

  }

}
