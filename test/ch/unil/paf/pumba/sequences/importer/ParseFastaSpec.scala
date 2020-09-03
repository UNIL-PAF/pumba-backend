package ch.unil.paf.pumba.sequences.importer

import java.io.File

import ch.unil.paf.pumba.protein.importer.ParsePeptides
import ch.unil.paf.pumba.protein.models.{OrganismName, ProteinId}
import ch.unil.paf.pumba.sequences.models.{DataBaseName, ProteinSequence}
import org.specs2.mutable.Specification

import scala.io.Source

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
class ParseFastaSpec extends Specification{

  val fastaFile = new File("test/resources/fasta/tiny_UP000005640_9606.fasta")
  val fastaFileIsoforms = new File("test/resources/fasta/tiny_uniprot-proteome_UP000005640.fasta")

  "parseHeader" should {

    val fastaHeader = "tr|A0A024R161|A0A024R161_HUMAN Guanine nucleotide-binding protein subunit gamma OS=Homo sapiens GN=DNAJC25-GNG10 PE=3 SV=1"
    val (proteinId, entryName, geneName, proteinName, isoformId) = ParseFasta().parseHeader(fastaHeader)

    "extract correct proteinId" in {
      proteinId.value mustEqual("A0A024R161")
    }

    "extract correct entryName" in {
      entryName.value mustEqual("A0A024R161_HUMAN")
    }

    "extract correct proteinName" in {
      proteinName.value mustEqual("Guanine nucleotide-binding protein subunit gamma")
    }

    "extract correct geneName" in {
      geneName.isDefined mustEqual(true)
      geneName.get.value mustEqual("DNAJC25-GNG10")
    }

    "not have an isoform" in {
      isoformId.isEmpty mustEqual(true)
    }

  }

  "parse" should {

    val peptideSequences: Seq[ProteinSequence]  = ParseFasta().parse(fastaFile, DataBaseName("test_db"), OrganismName("human")).toSeq

    "give correct number of ProteinSequences" in {
      peptideSequences.length mustEqual(142)
    }

    "contain correct ProteinSequences" in {
      val pepSeq = peptideSequences(0)
      pepSeq.sequence mustEqual("MGAPLLSPGWGAGAAGRRWWMLLAPLLPALLLVRPAGALVEGLYCGTRDCYEVLGVSRSAGKAEIARAYRQLARRYHPDRYRPQPGDEGPGRTPQSAEEAFLLVATAYETLKVSQAAAELQQYCMQNACKDALLVGVPAGSNPFREPRSCALL")
      pepSeq.length mustEqual(153)
      pepSeq.entryName.value mustEqual("A0A024R161_HUMAN")
      pepSeq.organismName.value mustEqual("human")
    }

  }

  "parse with isoforms" should {

    val peptideSequences: Seq[ProteinSequence]  = ParseFasta().parse(fastaFileIsoforms, DataBaseName("test_db_isoforms"), OrganismName("human")).toSeq

    "give correct number of ProteinSequences" in {
      peptideSequences.length mustEqual(44)
    }

    "contain correct ProteinSequences" in {
      val pepSeq = peptideSequences(41)
      pepSeq.isoformId mustEqual(Some(4))
      pepSeq.proteinId mustEqual(ProteinId("Q5JW98-4"))
      pepSeq.entryName.value mustEqual("CAHM4_HUMAN")
      pepSeq.geneName.get.value mustEqual("CALHM4")
      pepSeq.proteinName.value mustEqual("Isoform 4 of Calcium homeostasis modulator protein 4")
      pepSeq.sequence mustEqual("MAPRSAKETFRINPNVAANLSAPSDVILVRDEIALLHRYQSQMLGWILITLATIAALVSCCVAKCCSPLTSLQHCYWTSHLQNERELFEQAAEQHSRLLMMHRIKKLFGFIPGSEDVKHIRIPSCQDWKDISVPTLLCMGDDLQGHYSFLGNRVDEDNEEDRSRGIELKP")
      pepSeq.length mustEqual(170)
      pepSeq.organismName.value mustEqual("human")
      Math.round(pepSeq.molWeight) mustEqual(19305)
    }

  }

}
