package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models.Protein
import scala.io.Source
import play.api.Logger

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ParseProteinGroups {

  final val SEPARATOR = "\\t"

  def parseProteinGroupsTable(proteinGroupsFile: File, dataSetId: DataSetId): Iterator[Protein] = {
    if(! proteinGroupsFile.exists()){
      Logger.error("File does not exist")
      throw new Exception(s"File does not exist [${proteinGroupsFile.getName}].")
    }

    val proteinGroupsLines = Source.fromFile(proteinGroupsFile).getLines()
    val headers = parseHeaders(proteinGroupsLines.next)
    val intPos = getIntensityPositions(headers, "intensity.h.")

    for {
      line <- proteinGroupsLines
    } yield {
      lineToProtein(line, dataSetId, headers, intPos)
    }

  }

  def lineToProtein(line:String, dataSetId: DataSetId, headers: Map[String, Int], intPos: Seq[Int], sep: String = SEPARATOR) : Protein = {
    val values: Array[String] = line.split(sep)
    Protein(
      dataSetId = dataSetId,
      proteinIDs = parseNameField(values, headers, fieldName = "majority.protein.ids"),
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

  def parseHeaders(line: String, sep: String = SEPARATOR): Map[String, Int] = {
    val headers = line.split(sep)
    val cleanHeaders = headers.map(_.replaceAll("\\s", ".").toLowerCase)
    cleanHeaders.zipWithIndex.toMap
  }

}


object ParseProteinGroups {
  def apply() = new ParseProteinGroups()
}