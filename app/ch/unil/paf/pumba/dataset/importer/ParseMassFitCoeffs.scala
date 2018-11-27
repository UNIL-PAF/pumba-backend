package ch.unil.paf.pumba.dataset.importer

import java.io.File

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ParseMassFitCoeffs {

  val SEPERATOR = ","

  def parseCsvFile(csvFile: String): Array[Double] ={
    val fileContent: String = Source.fromFile(csvFile).getLines().mkString
    fileContent.split(SEPERATOR).map(_.toDouble)
  }

}

object ParseMassFitCoeffs {
  def apply() = new ParseMassFitCoeffs
}
