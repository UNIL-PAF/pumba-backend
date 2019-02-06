package ch.unil.paf.pumba.sequences.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.models._
import ch.unil.paf.pumba.protein.services.SequenceService
import ch.unil.paf.pumba.sequences.models.{DataBaseName, ProteinSequence}
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfter, _}
import play.api.test.Helpers.{await, _}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Roman Mylonas
  * copyright 2018-2019, Protein Analysis Facility UNIL
  */
class SequenceServiceSpec extends PlayWithMongoSpec with BeforeAndAfter {

  val sequenceService = new SequenceService(reactiveMongoApi)

  val proteinSeq = ProteinSequence(
    proteinId = ProteinId("A0A024R161"),
    entryName = ProteinEntryName("A0A024R161_HUMAN"),
    proteinName = ProteinName("Guanine nucleotide-binding protein subunit gamma"),
    organismName = OrganismName("Homo sapiens"),
    geneName = Some(GeneName("DNAJC25-GNG10")),
    dataBaseName = DataBaseName("test_db"),
    sequence = "MGAPLLSPGWGAGAAGRRWWMLLAPLLPALLLVRPAGALVEGLYCGTRDCYEVLGVSRSAGKAEIARAYRQLARRYHPDRYRPQPGDEGPGRTPQSAEEAFLLVATAYETLKVSQAAAELQQYCMQNACKDALLVGVPAGSNPFREPRSCALL",
    length = 153
  )

  before {
    //Init DB
    await(sequenceService.insert(proteinSeq))
  }

  after {
    //clean DB
    await(sequenceService.dropAll())
  }

  "SequenceService" should {

    "find a sequence" in {
      val protSeqs: List[ProteinSequence] = await(sequenceService.getSequences(proteinId = ProteinId("A0A024R161"), dataBaseName = DataBaseName("test_db")))
      protSeqs.length mustEqual(1)
      protSeqs(0).entryName.value mustEqual("A0A024R161_HUMAN")
    }

  }

}
