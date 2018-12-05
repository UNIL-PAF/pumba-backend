package ch.unil.paf.pumba.dataset.models

case class DataSet(
                    id: DataSetId,
                    name: String,
                    sample: Sample,
                    status: DataSetStatus,
                    message: Option[String],
                    massFitResult: Option[MassFitResult]
                  )


case class DataSetId(value: String) extends AnyVal


case class Sample(value: String) extends AnyVal