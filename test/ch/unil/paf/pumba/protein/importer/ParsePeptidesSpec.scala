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

    val pepMap: Map[MaxQuantPepId, Peptide] = ParsePeptides().parsePeptidesTable(peptidesFile)

    "should contain all peptides containing intensities" in {
      pepMap.keys.size mustEqual(314)
    }

    "every key should have a peptide" in {
      pepMap.values.size mustEqual(314)
    }

    "contain the correct data" in {
      val pep: Peptide = pepMap(MaxQuantPepId(2))

      pep.startPos mustEqual(2)
      pep.endPos mustEqual(26)
      pep.aminoAcidBefore mustEqual("M")
      pep.aminoAcidAfter mustEqual("K")
      pep.sequence mustEqual("AAAAAAAGDSDSWDADAFSVEDPVR")
      pep.theoMass.toInt mustEqual(2464)
      pep.isRazor mustEqual(None)
    }

  }

}
