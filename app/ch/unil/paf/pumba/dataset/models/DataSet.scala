package ch.unil.paf.pumba.dataset.models

case class DataSet(
                    id: DataSetId,
                    name: String,
                    sample: String,
                    status: DataSetStatus,
                    message: Option[String],
                    massFitResult: Option[MassFitResult]
                  )