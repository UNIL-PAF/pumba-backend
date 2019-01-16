package ch.unil.paf.pumba.protein.importer

import java.io.File

import ch.unil.paf.pumba.dataset.models.DataSetId
import ch.unil.paf.pumba.protein.models.{MaxQuantPepId, Peptide, Protein}
import play.api.Logger

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
class ParsePeptides {

  final val SEPARATOR = "\\t"

  def parsePeptidesTable(peptidesFile: File): Map[MaxQuantPepId, Peptide] = {
    if(! peptidesFile.exists()){
      Logger.error("File does not exist")
      throw new Exception(s"File does not exist [${peptidesFile.getName}].")
    }

    val peptidesLines = Source.fromFile(peptidesFile).getLines()
    val headers = parseHeaders(peptidesLines.next)
    val intPos = getIntensityPositions(headers, "intensity.h.")

    val pepMap: Iterator[(MaxQuantPepId, Peptide)] = for {
      line <- peptidesLines
      peptides <- lineToPeptides(line, headers, intPos)
    } yield {
      (peptides.maxQuantId, peptides)
    }

//    pepMap.foldLeft(Map.empty[MaxQuantPepId, Peptide], { case (a:Map[MaxQuantPepId, Peptide], (id: MaxQuantPepId, pep: Peptide)) =>
//      a.updated(id, pep)
//    })
    pepMap.toMap
  }

  def lineToPeptides(line:String, headers: Map[String, Int], intPos: Seq[Int], sep: String = SEPARATOR) : Seq[Peptide] = {
    val values: Array[String] = line.split(sep)

    // get actual intensities
    val ints: Seq[(Double, Int)] = intPos.zipWithIndex.map(p => (values(p._1).toDouble, p._2)).filter(_._1 > 0)

    ints.map{ i =>
      Peptide(
        maxQuantId = MaxQuantPepId(values(headers("id")).toInt),
        sequence = values(headers("sequence")),
        aminoAcidBefore = values(headers("amino.acid.before")),
        aminoAcidAfter = values(headers("amino.acid.after")),
        startPos = values(headers("start.position")).toInt,
        endPos = values(headers("end.position")).toInt,
        isRazor = None,
        sliceNr = i._2 + 1,
        theoMass = values(headers("mass")).toDouble
      )
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