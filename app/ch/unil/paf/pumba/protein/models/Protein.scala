package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models.{DataSet, DataSetId}

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
sealed trait BaseProtein {
  def proteinIDs: Seq[String]
  def geneNames: Seq[String]
  def theoMolWeight: Double
  def intensities: Seq[Double]
}


case class Protein (
                     dataSetId: DataSetId,
                     proteinIDs: Seq[String],
                     geneNames: Seq[String],
                     theoMolWeight: Double,
                     intensities: Seq[Double]
                   ) extends BaseProtein


case class ProteinWithDataSet (
                                proteinIDs: Seq[String],
                                geneNames: Seq[String],
                                theoMolWeight: Double,
                                intensities: Seq[Double],
                                dataSet: DataSet
                              )extends BaseProtein


object ProteinFactory {

  def apply(protein: Protein, dataSet: DataSet) : ProteinWithDataSet = ProteinWithDataSet(protein.proteinIDs,
    protein.geneNames, protein.theoMolWeight, protein.intensities, dataSet)

}