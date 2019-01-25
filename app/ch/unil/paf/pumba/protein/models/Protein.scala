package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId}

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */

case class ProteinId(value: String) extends AnyVal

sealed trait BaseProtein {
  def proteinIDs: Seq[ProteinId]
  def geneNames: Seq[String]
  def theoMolWeight: Double
  def intensities: Seq[Double]
  def peptides: Seq[Peptide]
}


case class Protein (
                     dataSetId: DataSetId,
                     proteinIDs: Seq[ProteinId],
                     geneNames: Seq[String],
                     theoMolWeight: Double,
                     intensities: Seq[Double],
                     peptides: Seq[Peptide]
                   ) extends BaseProtein


case class ProteinWithDataSet (
                                proteinIDs: Seq[ProteinId],
                                geneNames: Seq[String],
                                theoMolWeight: Double,
                                intensities: Seq[Double],
                                dataSet: DataSet,
                                peptides: Seq[Peptide]
                              )extends BaseProtein


object ProteinFactory {

  def apply(protein: Protein, dataSet: DataSet) : ProteinWithDataSet = ProteinWithDataSet(protein.proteinIDs,
    protein.geneNames, protein.theoMolWeight, protein.intensities, dataSet, protein.peptides)

}