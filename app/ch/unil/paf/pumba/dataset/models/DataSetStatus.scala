package ch.unil.paf.pumba.dataset.models

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

sealed trait DataSetStatus {def value: String}
case object DataSetCreated extends DataSetStatus {val value = "created"}
case object DataSetRunning extends DataSetStatus {val value = "running"}
case object DataSetDone extends DataSetStatus {val value = "done"}
case object DataSetError extends DataSetStatus {val value = "error"}

