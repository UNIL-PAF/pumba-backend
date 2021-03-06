package ch.unil.paf.pumba.common.rexec

import ch.unil.paf.pumba.dataset.models.{DataSetCreated, DataSetId, DataSetStatus}

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class TestChangeStatusCallback(id: DataSetId) extends ChangeStatusCallback {

  override def newStatus (status: DataSetStatus, message: Option[String] = None) = {
    TestChangeStatusCallback.lastStatus = status
    TestChangeStatusCallback.lastMessage = message
  }

  def getLastStatus(): DataSetStatus = TestChangeStatusCallback.lastStatus

  def getLastMessage(): String = TestChangeStatusCallback.lastMessage.getOrElse("")

}

object TestChangeStatusCallback {
  var lastStatus: DataSetStatus = DataSetCreated
  var lastMessage: Option[String] = None
}