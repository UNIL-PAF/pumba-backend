package ch.unil.paf.pumba.sequences.services

import ch.unil.paf.pumba.sequences.models.ProteinSequence
import reactivemongo.api.commands.WriteResult
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
class ImportSequences {

  def importSequences(sequences: Iterator[ProteinSequence], sequenceService: SequenceService)(implicit ec: ExecutionContext): Future[Int] = {
    sequences.foldLeft( Future{0} )( (futureInt: Future[Int], sequence: ProteinSequence) => {
      val futureRes: Future[WriteResult] = sequenceService.insert(sequence)
      for {
        res <- futureRes
        i <- futureInt
      } yield {
        (if(res.ok) 1 else 0) + i
      }
    })
  }

}

object ImportSequences {
  def apply() = new ImportSequences()
}

