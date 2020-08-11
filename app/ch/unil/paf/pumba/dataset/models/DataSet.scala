package ch.unil.paf.pumba.dataset.models

import ch.unil.paf.pumba.sequences.models.DataBaseName

case class DataSet(
                    id: DataSetId,
                    name: String,
                    sample: Sample,
                    status: DataSetStatus,
                    message: Option[String],
                    massFitResult: Option[MassFitResult],
                    dataBaseName: Option[DataBaseName],
                    colorGroup: Int,
                    organism: String
                  )


case class DataSetId(value: String) extends AnyVal


case class Sample(value: String) extends AnyVal