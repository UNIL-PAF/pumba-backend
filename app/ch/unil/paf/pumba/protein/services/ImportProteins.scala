package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.protein.models.Protein
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
  */
class ImportProteins {

  def importProteins(proteins: Iterator[Protein], proteinService: ProteinService)(implicit ec: ExecutionContext): Future[Int] = {
    proteins.foldLeft( Future{0} )( (futureInt: Future[Int], protein: Protein) => {
      val futureRes: Future[WriteResult] = proteinService.insertProtein(protein)
      for {
        res <- futureRes
        i <- futureInt
      } yield {
        (if(res.ok) 1 else 0) + i
      }
    })
  }

}

object ImportProteins {
  def apply() = new ImportProteins()
}