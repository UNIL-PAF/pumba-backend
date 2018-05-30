package ch.unil.paf.pumba.dataset.models

case class DataSet(id: String, status: DataSetStatus, proteinGroupsFile: Option[String])