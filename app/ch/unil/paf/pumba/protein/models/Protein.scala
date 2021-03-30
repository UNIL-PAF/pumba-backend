package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId}

/**
  * @author Roman Mylonas
  * copyright 2018-2021, Protein Analysis Facility UNIL
  */

case class ProteinId(value: String) extends AnyVal
case class GeneName(value: String) extends AnyVal
case class ProteinOrGene(value: String) extends AnyVal
case class ProteinEntryName(value: String) extends AnyVal
case class OrganismName(value: String) extends AnyVal
case class ProteinName(value: String) extends AnyVal

sealed trait BaseProtein {
  def proteinIDs: Seq[ProteinId]
  def geneNames: Seq[GeneName]
  def theoMolWeight: Double
  def intensities: Seq[Double]
  def peptides: Seq[Peptide]
}


case class Protein (
                     dataSetId: DataSetId,
                     proteinIDs: Seq[ProteinId],
                     geneNames: Seq[GeneName],
                     theoMolWeight: Double,
                     intensities: Seq[Double],
                     peptides: Seq[Peptide]
                   ) extends BaseProtein


case class ProteinWithDataSet (
                                proteinIDs: Seq[ProteinId],
                                geneNames: Seq[GeneName],
                                theoMolWeight: Double,
                                intensities: Seq[Double],
                                dataSet: DataSet,
                                peptides: Seq[Peptide]
                              )extends BaseProtein


object ProteinFactory {

  def apply(protein: Protein, dataSet: DataSet) : ProteinWithDataSet = ProteinWithDataSet(protein.proteinIDs,
    protein.geneNames, protein.theoMolWeight, protein.intensities, dataSet, protein.peptides)

}