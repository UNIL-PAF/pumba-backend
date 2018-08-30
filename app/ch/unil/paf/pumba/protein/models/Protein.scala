package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models.DataSetId

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */
case class Protein(
                    dataSetId: DataSetId,
                    proteinIDs: List[String],
                    geneNames: List[String],
                    theoMolWeight: Double,
                    intensities: List[Double]
                  )
