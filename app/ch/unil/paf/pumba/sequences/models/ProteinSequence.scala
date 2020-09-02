package ch.unil.paf.pumba.sequences.models

import ch.unil.paf.pumba.protein.models._

/**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
  */
case class ProteinSequence(
                            proteinId: ProteinId,
                            entryName: ProteinEntryName,
                            proteinName: ProteinName,
                            organismName: OrganismName,
                            geneName: Option[GeneName],
                            dataBaseName: DataBaseName,
                            isoformId: Option[Int],
                            sequence: String,
                            length: Int
                          )


case class DataBaseName(value: String) extends AnyVal
