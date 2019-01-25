package ch.unil.paf.pumba.sequences.models

import ch.unil.paf.pumba.protein.models.ProteinId

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
case class ProteinSequence(proteinId: ProteinId, sequence: String, length: Int)
