package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.protein.models.{MaxQuantPepId, Peptide}
import play.api.Logger

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
class ParsePeptides {

  final val SEPARATOR = "\\t"

  def parsePeptidesTable(peptidesFile: File): Map[MaxQuantPepId, Seq[Peptide]] = {
    if(! peptidesFile.exists()){
      Logger.error("File does not exist")
      throw new Exception(s"File does not exist [${peptidesFile.getName}].")
    }

    val peptidesLines = Source.fromFile(peptidesFile).getLines()
    val headers = parseHeaders(peptidesLines.next)
    val intPos = getIntensityPositions(headers, "intensity.h.")

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
    val aminoAcidBefore = values(headers("amino.acid.before"))
    val aminoAcidAfter = values(headers("amino.acid.after"))
    val startPos = values(headers("start.position")).toInt
    val endPos = values(headers("end.position")).toInt
    val isRazor = None
    val theoMass = Math.log10(values(headers("mass")).toDouble)

    ints.map{ i =>
      Peptide(maxQuantId, sequence, aminoAcidBefore, aminoAcidAfter, startPos, endPos, isRazor, sliceNr = i._2 + 1, theoMass)
    }
  }


  def parseHeaders(line: String, sep: String = SEPARATOR): Map[String, Int] = {
    val headers = line.split(sep)
    val cleanHeaders = headers.map(_.replaceAll("[^a-z|0-9|A-Z]", ".").toLowerCase)
    cleanHeaders.zipWithIndex.toMap
  }

  def getIntensityPositions(headers: Map[String, Int], intensityPatter: String): Seq[Int] = {
    val matchingKeys = headers.keys.filter(_.contains(intensityPatter))
    matchingKeys.map(headers(_)).toSeq.sorted
  }
}


object ParsePeptides {
  def apply() = new ParsePeptides()
}