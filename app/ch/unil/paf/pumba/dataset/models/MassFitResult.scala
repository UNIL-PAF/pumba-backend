package ch.unil.paf.pumba.dataset.models

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
case class MassFitResult(
                          massFitPicturePath: String,
                          massFitRData: String,
                          proteinGroupsPath: String,
                          peptidesPath: String,
                          massFitCoeffs: Array[Double],
                          massFits: Array[Double],
                          maxInt: Double,
                          normCorrFactor: Double
                        )