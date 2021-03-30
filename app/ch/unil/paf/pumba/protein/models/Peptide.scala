package ch.unil.paf.pumba.protein.models

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
  */
case class Peptide(
                    maxQuantId: MaxQuantPepId,
                    proteinIDs: Seq[ProteinId],
                    sequence: String,
                    aminoAcidBefore: Option[String],
                    aminoAcidAfter: Option[String],
                    startPos: Option[Int],
                    endPos: Option[Int],
                    isRazor: Option[Boolean],
                    sliceNr: Int,
                    theoMass: Double,
                    score: Double,
                    uniqueByGroup: Boolean,
                    intensity: Double
                  )

case class MaxQuantPepId(value: Int) extends AnyVal