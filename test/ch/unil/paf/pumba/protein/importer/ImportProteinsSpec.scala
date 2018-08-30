package ch.unil.paf.pumba.protein.importer

import java.io.File

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

}
