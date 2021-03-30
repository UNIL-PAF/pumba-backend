package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.protein.models.{MaxQuantPepId, Peptide}
import org.specs2.mutable.Specification

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
  */
class ParsePeptidesSpec extends Specification{

  "parseHeaders" should {

    val peptidesFile = new File("test/resources/max_quant/parse_peptides/peptides.txt")
    val headerLine:String = Source.fromFile(peptidesFile).getLines().next

    val mergedPeptidesFile = new File("test/resources/max_quant/parse_peptides/HEK293_11971_11973_12019_peptides.txt")
    val mergedHeaderLine:String = Source.fromFile(mergedPeptidesFile).getLines().next

    "give back correct HashMap" in {
      val headers: Map[String, Int] = ParsePeptides().parseHeaders(headerLine)
      headers.size mustEqual(581)
      headers("id") mustEqual(573)
    }

    "merged - give back correct HashMap" in {
      val headers: Map[String, Int] = ParsePeptides().parseHeaders(mergedHeaderLine)
      headers.size mustEqual(615)
      headers("id") mustEqual(606)
    }

    "get correct intensity positions" in {
      val headers: Map[String, Int] = ParsePeptides().parseHeaders(headerLine)
      val intPos = ParsePeptides().getIntensityPositions(headers, "intensity.h.")

      intPos.length mustEqual(47)
      intPos(0) mustEqual(432)
      intPos.last mustEqual(570)
    }

    "merged - get correct intensity positions" in {
      val headers: Map[String, Int] = ParsePeptides().parseHeaders(mergedHeaderLine)
      val intPos = ParsePeptides().getIntensityPositions(headers, "intensity.", sampleName = Some("12019"))

      intPos.length mustEqual(46)
      intPos(0) mustEqual(558)
      intPos.last mustEqual(603)
    }

  }

  "parsePeptidesTable" should {

    val peptidesFile = new File("test/resources/max_quant/parse_peptides/peptides.txt")
    val pepMap: Map[MaxQuantPepId, Seq[Peptide]] = ParsePeptides().parsePeptidesTable(peptidesFile)

    "should contain all peptides containing intensities" in {
      pepMap.keys.size mustEqual(314)
    }

    "every key should have a peptide list" in {
      pepMap.values.size mustEqual(314)
    }

    "we should have more peptides than keys" in {
      val pepListOfList: Seq[Seq[Peptide]] = pepMap.values.toSeq
      pepListOfList.flatten.length mustEqual(966)
    }

    "first entry should have 3 peptides" in {
      pepMap(MaxQuantPepId(2)).length mustEqual(3)
    }

    "first peptide contain the correct data" in {
      val pep1: Peptide = pepMap(MaxQuantPepId(2))(0)

      pep1.startPos mustEqual(Some(2))
      pep1.endPos mustEqual(Some(26))
      pep1.aminoAcidBefore mustEqual(Some("M"))
      pep1.aminoAcidAfter mustEqual(Some("K"))
      pep1.sequence mustEqual("AAAAAAAGDSDSWDADAFSVEDPVR")
      pep1.theoMass.toInt mustEqual(3)
      pep1.isRazor mustEqual(None)
      pep1.sliceNr mustEqual(32)
      pep1.score mustEqual(156.87)
      pep1.uniqueByGroup mustEqual(true)
      pep1.proteinIDs.length mustEqual(1)
      pep1.proteinIDs(0).value mustEqual("O75822")
    }

    "second peptide contain the correct data" in {
      val pep2: Peptide = pepMap(MaxQuantPepId(2))(1)

      pep2.startPos mustEqual(Some(2))
      pep2.endPos mustEqual(Some(26))
      pep2.aminoAcidBefore mustEqual(Some("M"))
      pep2.aminoAcidAfter mustEqual(Some("K"))
      pep2.sequence mustEqual("AAAAAAAGDSDSWDADAFSVEDPVR")
      pep2.theoMass.toInt mustEqual(3)
      pep2.isRazor mustEqual(None)
      pep2.sliceNr mustEqual(33)
      pep2.score mustEqual(156.87)
      pep2.uniqueByGroup mustEqual(true)
      pep2.proteinIDs.length mustEqual(1)
      pep2.proteinIDs(0).value mustEqual("O75822")
    }

    "peptide from other protein contain the correct data" in {
      val pep2: Peptide = pepMap(MaxQuantPepId(593))(0)

      pep2.proteinIDs.length mustEqual(2)
      pep2.proteinIDs(0).value mustEqual("P35579")
      pep2.proteinIDs(1).value mustEqual("P35580")
      pep2.startPos mustEqual(Some(581))
      pep2.endPos mustEqual(Some(587))
      pep2.aminoAcidBefore mustEqual(Some("K"))
      pep2.aminoAcidAfter mustEqual(Some("N"))
      pep2.sequence mustEqual("ADEWLMK")
      pep2.theoMass.toInt mustEqual(2)
      pep2.isRazor mustEqual(None)
      pep2.sliceNr mustEqual(8)
      pep2.score mustEqual(58.981)
      pep2.uniqueByGroup mustEqual(false)
    }

  }

}
