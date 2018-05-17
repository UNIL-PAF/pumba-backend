package ch.unil.paf.pumba.dataset.importer

import java.io.File

import common.helpers.Unzip
import org.specs2.mutable.Specification

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
class ImportMQDataSpec extends Specification {

  val destPath = Unzip.createTmpDir

  "unzip file" in {

    val zipFile = new File("test/resources/dataset/Conde_9508_sub.zip")
    val txtDir: File = ImportMQData().unpackZip(zipFile, destPath, false)

    txtDir.isDirectory mustEqual true
    txtDir.getName mustEqual("txt")

  }
}