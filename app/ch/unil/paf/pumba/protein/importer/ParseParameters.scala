package ch.unil.paf.pumba.protein.importer

import java.io.{File, FileNotFoundException}

import ch.unil.paf.pumba.protein.models.{MaxQuantPepId, Peptide}
import ch.unil.paf.pumba.sequences.models.DataBaseName
import play.api.Logger

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
  */
class ParseParameters {

  final val SEPARATOR = "\\t"

  def parseTable(parametersFile: File):DataBaseName = {
    if(! parametersFile.exists()){
      Logger.error("File does not exist")
      throw new FileNotFoundException(s"File does not exist [${parametersFile.getName}].")
    }

    val paramsLines = Source.fromFile(parametersFile).getLines().toSeq

    // get the correct line
    val dataBaseLine: String = paramsLines.find(_.contains("Fasta")) match {
      case Some(i) => i
      case None => throw new Exception(s"Could not parse [Fasta file] from [${parametersFile.getName}].")
    }

    // parse the name from the line
    val rDataBaseName = """.+\\(.+)\.fasta""".r

    val dataBaseName: String = dataBaseLine match {
      case rDataBaseName(dataBaseName) => dataBaseName
      case _ => throw new Exception(s"Could not parse DataBaseName from [$dataBaseLine].")
    }

    DataBaseName(dataBaseName)
  }
}


object ParseParameters {
  def apply() = new ParseParameters()
}