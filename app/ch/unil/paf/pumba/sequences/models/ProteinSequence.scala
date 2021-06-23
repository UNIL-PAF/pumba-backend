package ch.unil.paf.pumba.sequences.models

import ch.unil.paf.pumba.protein.models._

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
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
                            length: Int,
                            molWeight: Double
                          )


case class DataBaseName(value: String) extends AnyVal

case class ProteinSequenceString(
                                proteinId: ProteinId,
                                geneName: Option[GeneName],
                                string: String
                                )
