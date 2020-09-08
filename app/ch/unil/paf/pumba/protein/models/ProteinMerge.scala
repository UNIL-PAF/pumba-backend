package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models.Sample
import ch.unil.paf.pumba.sequences.models.ProteinSequence

/**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
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
  * @param sample
  * @param theoMergedProtein
  * @param proteins
  */
case class ProteinMerge(mainProteinId: ProteinId, sample: Sample, theoMergedProtein: TheoMergedProtein, proteins: Seq[ProteinWithDataSet])

/**
  * Contains the merged proteins and the sequences
  * @param proteinMerges
  * @param sequences
  */
case class ProteinMergeWithSequence(proteinMerges: Seq[ProteinMerge], sequences: Seq[ProteinSequence])


