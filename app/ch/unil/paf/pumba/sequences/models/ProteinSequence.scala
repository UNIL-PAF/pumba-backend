package ch.unil.paf.pumba.sequences.models

import ch.unil.paf.pumba.protein.models._

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
case class ProteinSequence(
                            proteinId: ProteinId,
                            entryName: ProteinEntryName,
                            proteinName: ProteinName,
                            organismName: OrganismName,
                            geneName: Option[GeneName],
                            dataBaseName: DataBaseName,
                            sequence: String,
                            length: Int
                          )


case class DataBaseName(value: String) extends AnyVal
