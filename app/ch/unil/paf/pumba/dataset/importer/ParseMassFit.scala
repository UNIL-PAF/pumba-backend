package ch.unil.paf.pumba.dataset.importer

import java.io.File

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ParseMassFit {

  val SEPERATOR = ","

  def parseCsvCoeffs(csvFile: String): Array[Double] ={
    val fileContent: String = Source.fromFile(csvFile).getLines().mkString
    fileContent.split(SEPERATOR).map(_.toDouble)
  }

  def parseCsvFits(csvFile: String): Array[Double] ={
    val fileContent: String = Source.fromFile(csvFile).getLines().mkString
    fileContent.split(SEPERATOR).map(_.toDouble)
  }

}

object ParseMassFit {
  def apply() = new ParseMassFit
}
