package ch.unil.paf.pumba.dataset.models

case class DataSet(
                    id: DataSetId,
                    name: String,
                    cellLine: String,
                    status: DataSetStatus,
                    message: Option[String],
                    massFitResult: Option[MassFitResult]
                  )