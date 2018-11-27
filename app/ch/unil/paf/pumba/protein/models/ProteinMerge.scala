package ch.unil.paf.pumba.protein.models

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */

/**
  * A theoretical protein created from the merge of several proteins
  * @param name
  * @param theoMolWeights
  * @param intensities
  */
case class TheoMergedProtein(name: String, theoMolWeights: Seq[Double], intensities: Seq[Double])

/**
  * Contains the merged protein together with all its original proteins
  * @param mainProteinId The proteinId from the user request
  * @param theoMergedProtein
  * @param proteins
  */
case class ProteinMerge(mainProteinId: String, theoMergedProtein: TheoMergedProtein, proteins: Seq[ProteinWithDataSet])



