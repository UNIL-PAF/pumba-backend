package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.protein.models.{MaxQuantPepId, Peptide}
import org.specs2.mutable.Specification

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
class ParsePeptidesSpec extends Specification{

  val peptidesFile = new File("test/resources/max_quant/parse_peptides/peptides.txt")

  "parseHeaders" should {

    val headerLine:String = Source.fromFile(peptidesFile).getLines().next

    "give back correct HashMap" in {
      val headers: Map[String, Int] = ParsePeptides().parseHeaders(headerLine)
      headers.size mustEqual(581)
      headers("id") mustEqual(573)
    }

    "get correct intensity positions" in {
      val headers: Map[String, Int] = ParsePeptides().parseHeaders(headerLine)
      val intPos = ParsePeptides().getIntensityPositions(headers, "intensity.h.")

      intPos.length mustEqual(47)
      intPos(0) mustEqual(432)
      intPos.last mustEqual(570)
    }

  }

  "parsePeptidesTable" should {

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

      pep1.startPos mustEqual(2)
      pep1.endPos mustEqual(26)
      pep1.aminoAcidBefore mustEqual("M")
      pep1.aminoAcidAfter mustEqual("K")
      pep1.sequence mustEqual("AAAAAAAGDSDSWDADAFSVEDPVR")
      pep1.theoMass.toInt mustEqual(2464)
      pep1.isRazor mustEqual(None)
      pep1.sliceNr mustEqual(32)
    }

    "second peptide contain the correct data" in {
      val pep2: Peptide = pepMap(MaxQuantPepId(2))(1)

      pep2.startPos mustEqual(2)
      pep2.endPos mustEqual(26)
      pep2.aminoAcidBefore mustEqual("M")
      pep2.aminoAcidAfter mustEqual("K")
      pep2.sequence mustEqual("AAAAAAAGDSDSWDADAFSVEDPVR")
      pep2.theoMass.toInt mustEqual(2464)
      pep2.isRazor mustEqual(None)
      pep2.sliceNr mustEqual(33)
    }

  }

}
