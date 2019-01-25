package ch.unil.paf.pumba.sequences.importer

import java.io.File

import ch.unil.paf.pumba.protein.importer.ParsePeptides
import ch.unil.paf.pumba.sequences.models.ProteinSequence
import org.specs2.mutable.Specification

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
class ParseFastaSpec extends Specification{

  val fastaFile = new File("test/resources/fasta/tiny_UP000005640_9606.fasta")

  "parseHeader" should {

    val fastaHeader = "tr|A0A024R161|A0A024R161_HUMAN Guanine nucleotide-binding protein subunit gamma OS=Homo sapiens GN=DNAJC25-GNG10 PE=3 SV=1"
    val (proteinId, entryName, geneName, organismName, proteinName) = ParseFasta().parseHeader(fastaHeader)

    "extract correct proteinId" in {
      proteinId.value mustEqual("A0A024R161")
    }

    "extract correct entryName" in {
      entryName.value mustEqual("A0A024R161_HUMAN")
    }

    "extract correct organismName" in {
      organismName.value mustEqual("Homo sapiens")
    }

    "extract correct proteinName" in {
      proteinName.value mustEqual("Guanine nucleotide-binding protein subunit gamma")
    }

    "extract correct geneName" in {
      geneName.isDefined mustEqual(true)
      geneName.get.value mustEqual("DNAJC25-GNG10")
    }

  }

  "parse" should {

    "give back correct HashMap" in {
      val peptideSequences: Seq[ProteinSequence]  = ParseFasta().parse(fastaFile).toSeq
      peptideSequences.length mustEqual(142)
    }

  }

}
