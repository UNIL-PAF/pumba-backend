package ch.unil.paf.pumba.common.helpers

import java.io.File
import org.specs2.mutable.Specification

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class UnZipSpecs extends Specification {

  "unzip file into tmp dir" in {

    val zipFile = new File("test/resources/common/tiny.zip")
    val unzippedDir = Unzip.unzipIntoTmp(zipFile)

    val files = FileFinder.getListOfDirs(unzippedDir)

    files.length mustEqual (1)
    files(0).getName mustEqual ("txt")

  }
}