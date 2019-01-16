package ch.unil.paf.pumba.protein.models

/**
  * @author Roman Mylonas
  *         copyright 2018-2019, Protein Analysis Facility UNIL
  */
case class Peptide(
                    maxQuantId: MaxQuantPepId,
                    sequence: String,
                    aminoAcidBefore: String,
                    aminoAcidAfter: String,
                    startPos: Int,
                    endPos: Int,
                    isRazor: Option[Boolean],
                    sliceNr: Int,
                    theoMass: Double
                  )

case class MaxQuantPepId(value: Int) extends AnyVal