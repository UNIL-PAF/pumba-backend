package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models.Protein
import ch.unil.paf.pumba.protein.services.ProteinService

import scala.concurrent.Future
import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ImportProteins {

  final val SEPERATOR = "\\t"

  def parseProteinGroupsTable(proteinGroupsFile: File, dataSetId: DataSetId): Iterator[Protein] = {
    val proteinGroupsLines = Source.fromFile(proteinGroupsFile).getLines()

    val headers = proteinGroupsLines.next

    for(line <- proteinGroupsLines){

    }

    ???
  }


  def getIntensityPositions(headers: Map[String, Int], intensityPatter: String): Seq[Int] = {
    val matchingKeys = headers.keys.filter(_.contains(intensityPatter))
    matchingKeys.map(headers(_)).toSeq.sorted
  }


  def parseHeaders(line: String, sep: String = SEPERATOR): Map[String, Int] = {
    val headers = line.split(sep)
    val cleanHeaders = headers.map(_.replaceAll("\\s", ".").toLowerCase)
    cleanHeaders.zipWithIndex.toMap
  }


}


object ImportProteins {
  def apply() = new ImportProteins()
}