package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.protein.models.{Peptide, Protein, ProteinId, ProteinWithDataSet}
import play.api.Logger

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
  */
class PeptideMatchService {

  def remapPeptides(protein: ProteinWithDataSet, proteinId: ProteinId, seq: String): ProteinWithDataSet = {

    val filteredPeps = protein.peptides.filter(p => p.proteinIDs.contains(proteinId))

    def getProteinIntensies(filteredPeps: Seq[Peptide], nrIntensities: Int, normCorrFactor: Double): Seq[Double] = {
        val proteinIntensities: Seq[Double] = filteredPeps.foldLeft(Array.fill(nrIntensities){0d}){ (ints: Array[Double], pep: Peptide) =>
          val i = pep.sliceNr - 1
          ints(i) = ints(i) + pep.intensity
          ints
        }.toSeq
        proteinIntensities.map(_/normCorrFactor)
    }

    val proteinIntensities = getProteinIntensies(filteredPeps, protein.intensities.length, protein.dataSet.massFitResult.get.normCorrFactor)

    val remapedPeps = filteredPeps.map( remapPeptide(_, seq, proteinId))
    protein.copy(peptides = remapedPeps.filter(_.startPos.isDefined), intensities = proteinIntensities)
  }

  def remapPeptide(peptide: Peptide, seq: String, proteinId: ProteinId): Peptide = {
      val startIndex = seq.indexOfSlice(peptide.sequence)
      val pepLen = peptide.sequence.length
      if(startIndex >= 0){
        peptide.copy(
          startPos = Some(startIndex + 1),
          endPos = Some(startIndex + pepLen),
          aminoAcidBefore = if(startIndex > 0) Some(seq.charAt(startIndex - 1).toString) else None,
          aminoAcidAfter = if(startIndex + pepLen < seq.length) Some(seq.charAt(startIndex + pepLen).toString) else None
        )
      }else{
        Logger.info(s"Warning: Could not map peptide [${peptide.maxQuantId}] to protein [${proteinId.value}].")
        peptide.copy(startPos = None)
      }
  }

}

object PeptideMatchService {
  def apply() = new PeptideMatchService()
}
