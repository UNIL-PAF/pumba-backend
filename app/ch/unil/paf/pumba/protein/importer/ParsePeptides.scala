package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.protein.models.{MaxQuantPepId, Peptide, ProteinId}
import play.api.Logger

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
  */
class ParsePeptides {

  final val SEPARATOR = "\\t"

  def parsePeptidesTable(peptidesFile: File, sampleName: Option[String] = None): Map[MaxQuantPepId, Seq[Peptide]] = {
    if(! peptidesFile.exists()){
      Logger.error("File does not exist")
      throw new Exception(s"File does not exist [${peptidesFile.getName}].")
    }

    val peptidesLines = Source.fromFile(peptidesFile).getLines()
    val headers = parseHeaders(peptidesLines.next)

    // depending on whether we have silac data or not we have the "h." or not.
    val intPosTmp = getIntensityPositions(headers, "intensity.h.", sampleName)
    val intPos = if (intPosTmp.length > 10) intPosTmp else getIntensityPositions(headers, "intensity.", sampleName)

    val pepMap: Iterator[(MaxQuantPepId, Seq[Peptide])] = peptidesLines.map{ line: String =>
      val peptides: Seq[Peptide] = lineToPeptides(line, headers, intPos)

      // give entries with no valid peptides a negative MaxQuantPepId
      if(peptides.isEmpty){
        (MaxQuantPepId(-1), Seq.empty[Peptide])
      }else{
        (peptides(0).maxQuantId, peptides)
      }
    }
    pepMap.filter(_._1.value > 0).toMap
  }

  def lineToPeptides(line:String, headers: Map[String, Int], intPos: Seq[Int], sep: String = SEPARATOR) : Seq[Peptide] = {
    val values: Array[String] = line.split(sep)

    // remove reverse entries
    if((values(headers("reverse")) == "+")) return Seq.empty[Peptide]

    // get actual intensities
    val ints: Seq[(Double, Int)] = intPos.zipWithIndex.map(p => (values(p._1).toDouble, p._2)).filter(_._1 > 0)

    val maxQuantId = MaxQuantPepId(values(headers("id")).toInt)
    val sequence = values(headers("sequence"))

    val rawStartPos = values(headers("start.position"))
    val startPos = if(rawStartPos == "") None else Some(rawStartPos.toInt)
    val endPos = if(rawStartPos == "") None else Some(values(headers("end.position")).toInt)
    val aminoAcidBefore = if(rawStartPos == "") None else Some(values(headers("amino.acid.before")))
    val aminoAcidAfter = if(rawStartPos == "") None else Some(values(headers("amino.acid.after")))

    // we have to parse the razor information from the proteinGroups.txt table.
    val isRazor = None
    val theoMass = Math.log10(values(headers("mass")).toDouble)
    val score = values(headers("score")).toDouble
    val uniqueByGroup = if(values(headers("unique..groups.")) == "yes") true else false
    val proteinIds: Seq[ProteinId] = values(headers("proteins")).split(";").map(ProteinId(_))

    ints.map{ i =>
      Peptide(maxQuantId, proteinIds, sequence, aminoAcidBefore, aminoAcidAfter, startPos, endPos, isRazor, sliceNr = i._2 + 1, theoMass, score, uniqueByGroup, i._1)
    }
  }

  def parseHeaders(line: String, sep: String = SEPARATOR): Map[String, Int] = {
    val headers = line.split(sep)
    val cleanHeaders = headers.map(_.replaceAll("[^a-z|0-9|A-Z]", ".").toLowerCase)
    cleanHeaders.zipWithIndex.toMap
  }

  def getIntensityPositions(headers: Map[String, Int], intensityPatter: String, sampleName: Option[String] = None): Seq[Int] = {
    val matchingKeys = headers.keys.filter(h => h.contains(intensityPatter) && (sampleName.isEmpty || h.contains(sampleName.get)))
    matchingKeys.map(headers(_)).toSeq.sorted
  }
}


object ParsePeptides {
  def apply() = new ParsePeptides()
}