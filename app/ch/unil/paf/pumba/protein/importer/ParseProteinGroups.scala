package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models._

import scala.io.Source
import play.api.Logger

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ParseProteinGroups {

  final val SEPARATOR = "\\t"

  def parseProteinGroupsTable(proteinGroupsFile: File, dataSetId: DataSetId, peptideMap: Map[MaxQuantPepId, Seq[Peptide]]): Iterator[Protein] = {
    if(! proteinGroupsFile.exists()){
      Logger.error("File does not exist")
      throw new Exception(s"File does not exist [${proteinGroupsFile.getName}].")
    }

    val proteinGroupsLines = Source.fromFile(proteinGroupsFile).getLines()
    val headers = parseHeaders(proteinGroupsLines.next)
    val intPos = getIntensityPositions(headers, "intensity.norm.")

    for {
      line <- proteinGroupsLines
    } yield {
      lineToProtein(line, dataSetId, headers, intPos, peptideMap)
    }

  }

  def lineToProtein(line:String, dataSetId: DataSetId, headers: Map[String, Int], intPos: Seq[Int], peptideMap: Map[MaxQuantPepId, Seq[Peptide]], sep: String = SEPARATOR) : Protein = {
    val values: Array[String] = line.split(sep)

    // extract peptides
    val peptideIds: Seq[MaxQuantPepId] = parseNameField(values, headers, fieldName = "peptide.ids").map( v => MaxQuantPepId(v.toInt))
    val peptideIsRazor: Seq[Boolean] = parseNameField(values, headers, fieldName = "peptide.is.razor").map( v => v == "True")
    val peptides = if(peptideMap.nonEmpty) parsePeptide(peptideMap, peptideIds, peptideIsRazor) else Seq.empty[Peptide]

    Protein(
      dataSetId = dataSetId,
      proteinIDs = parseNameField(values, headers, fieldName = "majority.protein.ids").map(ProteinId(_)),
      geneNames = parseNameField(values, headers, fieldName = "gene.names").map(GeneName(_)),
      theoMolWeight = values(headers("mol..weight..kda.")).toDouble,
      intensities = intPos.map(values(_).toDouble),
      peptides = peptides
    )
  }

  def parsePeptide(peptideMap: Map[MaxQuantPepId, Seq[Peptide]], peptideIds: Seq[MaxQuantPepId], peptideIsRazor: Seq[Boolean]): Seq[Peptide] = {
    // ignore peptides for which there was no valid entry in the peptides.txt
    val fltPepIds = peptideIds.filter(peptideMap.contains(_))

    fltPepIds.zipWithIndex.map { case (mqId: MaxQuantPepId, idx: Int) =>
      peptideMap(mqId).map(_.copy(isRazor = Some(peptideIsRazor(idx))))
    }.flatten
  }

  def parseNameField(values: Array[String], headers: Map[String, Int], fieldName: String): Seq[String] = {
    val headerPos: Option[Int] = if(headers.contains(fieldName)) Some(headers(fieldName)) else None
    if(headerPos.isDefined){
      val proteinIDsString = values(headers(fieldName))
      proteinIDsString.split(";").toSeq
    } else {
      Seq.empty[String]
    }
  }

  def getIntensityPositions(headers: Map[String, Int], intensityPatter: String): Seq[Int] = {
    val matchingKeys = headers.keys.filter(_.contains(intensityPatter))
    matchingKeys.map(headers(_)).toSeq.sorted
  }

  def parseHeaders(line: String, sep: String = SEPARATOR): Map[String, Int] = {
    val headers = line.split(sep)
    val cleanHeaders = headers.map(_.replaceAll("[^a-z|0-9|A-Z]", ".").toLowerCase)
    cleanHeaders.zipWithIndex.toMap
  }

}

object ParseProteinGroups {
  def apply() = new ParseProteinGroups()
}