package ch.unil.paf.pumba.sequences.importer

import java.io.File
import java.util.Scanner

import ch.unil.paf.pumba.protein.models._
import ch.unil.paf.pumba.sequences.models.{DataBaseName, ProteinSequence}
import org.biojava.nbio.aaproperties.PeptideProperties

/**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
  */
class ParseFasta {

  /**
    * parse the given source and produce an iterator of FastaEntry
    * @return
    */
  def parse(file: File, dataBaseName: DataBaseName, organismName: OrganismName): Iterator[ProteinSequence] = {

    val scanner = new Scanner(file).useDelimiter( """\n>""")

    //we build an iterator over the file
    val it: Iterator[String] = new Iterator[String] {
      override def hasNext: Boolean = scanner.hasNext

      override def next(): String = scanner.next()
    }

    it.map(parseOneProtBlock(_, dataBaseName, organismName))

  }

  def parseHeader(headline: String): (ProteinId, ProteinEntryName, Option[GeneName], ProteinName, Option[Int]) = {
    val rHeader = """^\w+\|(.+)\|(.+?)\s(.+?)\s+(\w{2}=.+)""".r

    headline match {
      case rHeader(proteinId, proteinEntry, proteinName, rest) => {

        val proteinIdSplit = proteinId.split("-")
        val isoformId = if(proteinIdSplit.length == 2) Some(proteinIdSplit(1).toInt) else None

        val restMap = rest.split("\\s(?=\\w{2}=)").map(_.split("=")).map(x => (x(0), x(1))).toMap

        val geneName = if(restMap.contains("GN")) Some(GeneName(restMap("GN"))) else None

        (ProteinId(proteinId), ProteinEntryName(proteinEntry), geneName, ProteinName(proteinName), isoformId)
      }

      case _ => throw new Exception(s"Failed to parse header: [$headline].")
    }
  }


  def parseOneProtBlock(protLines: String, dataBaseName: DataBaseName, organismName: OrganismName): ProteinSequence = {
    val firstNewLineIndex = protLines.indexOf("\n")
    // give back next entry and remove heading '>' and any special characters
    val headline = protLines.substring(0, firstNewLineIndex).replaceAll("^>|[^\\x00-\\x7F]", "")
    val seqLines = protLines.substring(firstNewLineIndex + 1)

    //get accession code and cleanup sequence
    val (proteinId, entryName, geneName, proteinName, isoformId) = parseHeader(headline)
    val seq = seqLines.replaceAll( """\s+""", "")

    // compute mol mass using BioJava
    val molMass: Double = PeptideProperties.getMolecularWeight(seq)

    ProteinSequence(proteinId, entryName, proteinName, organismName, geneName, dataBaseName, isoformId, seq, seq.length, molMass)
  }

}


object ParseFasta {
  def apply() = new ParseFasta()
}