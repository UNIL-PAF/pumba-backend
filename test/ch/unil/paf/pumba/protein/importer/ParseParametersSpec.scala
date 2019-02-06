package ch.unil.paf.pumba.protein.importer

import java.io.{File, FileNotFoundException}

import ch.unil.paf.pumba.common.helpers.DatabaseException
import ch.unil.paf.pumba.sequences.models.DataBaseName
import org.specs2.mutable.Specification

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
class ParseParametersSpec extends Specification{

  val parametersFile = new File("test/resources/max_quant/parameters.txt")

  "parse file" should {

    val dataBaseName: DataBaseName = ParseParameters().parseTable(parametersFile)

    "should give back correct dataBase name" in {
      dataBaseName.value mustEqual("UP000005640_9606")
    }

  }

  "parse not existent file" should {
    ParseParameters().parseTable(new File("not_existing")) must throwA[FileNotFoundException]
  }

}
