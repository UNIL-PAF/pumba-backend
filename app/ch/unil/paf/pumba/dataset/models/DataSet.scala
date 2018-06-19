package ch.unil.paf.pumba.dataset.models

case class DataSet(
                    id: DataSetId,
                    status: DataSetStatus,
                    message: Option[String],
                    massFitResult: Option[MassFitResult]
                  )