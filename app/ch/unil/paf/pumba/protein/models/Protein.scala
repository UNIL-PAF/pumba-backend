package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models.DataSetId

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
case class Protein(
                    dataSetId: DataSetId,
                    proteinIDs: List[String],
                    geneNames: List[String],
                    theoMolWeight: Double,
                    intensities: List[Double]
                  )
