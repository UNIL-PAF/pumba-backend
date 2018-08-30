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

    val headers = parseHeaders(proteinGroupsLines.next)

    println(headers.keys)

    val intPos = getIntensityPositions(headers, "intensity.h.")

    for {
      line <- proteinGroupsLines
    } yield {
      lineToProtein(line, dataSetId, headers, intPos)
    }

  }

  def lineToProtein(line:String, dataSetId: DataSetId, headers: Map[String, Int], intPos: Seq[Int], sep: String = SEPERATOR) : Protein = {
    val values: Array[String] = line.split(sep)
    Protein(
      dataSetId = dataSetId,
      proteinIDs = parseNameField(values, headers, "majority.protein.ids"),
      geneNames = parseNameField(values, headers, fieldName = "gene.names"),
      theoMolWeight = values(headers("mol..weight.[kda]")).toDouble,
      intensities = intPos.map(values(_).toDouble)
    )
  }

  def parseNameField(values: Array[String], headers: Map[String, Int], fieldName: String): Seq[String] = {
    val proteinIDsString = values(headers(fieldName))
    proteinIDsString.split(";").toSeq
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