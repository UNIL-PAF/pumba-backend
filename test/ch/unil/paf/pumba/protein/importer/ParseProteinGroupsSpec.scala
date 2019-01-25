package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models.{MaxQuantPepId, Peptide, Protein, ProteinId}
import org.specs2.mutable.Specification

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ParseProteinGroupsSpec extends Specification{

  val proteinGroupsFile = new File("test/resources/dataset/mass_fit_res/normalizedProteinGroups.txt")

  "parseHeaders" should {

    val headerLine:String = Source.fromFile(proteinGroupsFile).getLines().next

    "give back correct HashMap" in {

      val headers: Map[String, Int] = ParseProteinGroups().parseHeaders(headerLine)
      headers.size mustEqual(49)
      headers("majority.protein.ids") mustEqual(0)

    }

    "get correct intensity positions" in {
      val headers: Map[String, Int] = ParseProteinGroups().parseHeaders(headerLine)
      val intPos = ParseProteinGroups().getIntensityPositions(headers, "intensity.norm.")

      intPos.length mustEqual(46)
      intPos(0) mustEqual(3)
      intPos.last mustEqual(48)
    }

  }

  "parseProteinGroupsTable" should {

    val proteinsList: Seq[Protein] = ParseProteinGroups().parseProteinGroupsTable(proteinGroupsFile, DataSetId("dummy_id"), Map.empty[MaxQuantPepId, Seq[Peptide]]).toList

    "get correct number of proteins" in {
      proteinsList.length mustEqual(5015)
    }

    "get correct protein content" in {
      val protein = proteinsList(0)
      protein.intensities(0) mustEqual(0)
      protein.intensities(45) mustEqual(2.41416499001986E-7)
      protein.proteinIDs.length mustEqual(2)
      protein.proteinIDs(1).value mustEqual("P50151")
      protein.geneNames.length mustEqual(2)
      protein.geneNames(0) mustEqual("hCG_1994888")
      protein.dataSetId.value mustEqual("dummy_id")
      protein.theoMolWeight mustEqual(16.499)
    }

  }

  "parse proteinGroupsTable using a peptideMap" should {

    val pepProteinGroupsFile = new File("test/resources/max_quant/parse_peptides/normalizedProteinGroups.txt")
    val peptideFile = new File("test/resources/max_quant/parse_peptides/peptides.txt")
    val pepMap = ParsePeptides().parsePeptidesTable(peptideFile)
    val proteins = ParseProteinGroups().parseProteinGroupsTable(pepProteinGroupsFile, DataSetId("pep_parsed"), pepMap).toSeq

    "get correct number of proteins" in {
      proteins.length mustEqual(72)
    }

    "A0A024R216 should have 13 proteins" in {
      val prots: Seq[Protein] = proteins.filter(_.proteinIDs.contains(ProteinId("A0A024R216")))
      prots.length mustEqual(1)
      val prot = prots(0)
      prot.peptides.length mustEqual(13)
    }

    "one peptide should have correct data" in {
      val prots: Seq[Protein] = proteins.filter(_.proteinIDs.contains(ProteinId("A0A024R216")))
      prots.length mustEqual(1)
      val peptide1 = prots(0).peptides(0)
      val peptide2 = prots(0).peptides(1)

      peptide1.sequence mustEqual("ALLAEGVILR")
      peptide2.sequence mustEqual("ALLAEGVILR")

      peptide1.sliceNr mustEqual(38)
      peptide2.sliceNr mustEqual(39)
    }

  }

}
