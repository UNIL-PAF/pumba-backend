package ch.unil.paf.pumba.common.rexec
import ch.unil.paf.pumba.dataset.models.{DataSetId, DataSetStatus}

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
class DummyChangeStatusCallback(id: DataSetId) extends ChangeStatusCallback {

  override def newStatus(status: DataSetStatus, message: Option[String] = None) = {
    println("update status of [" + id.value + "] to [" + status.value + "]")
    if(message.nonEmpty) println(message.get)
  }

}
