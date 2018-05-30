package ch.unil.paf.pumba.common.rserve

import akka.actor._

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

object RserveActor {
  def props(changeStatusCallback: ChangeStatusCallback) = Props[RserveActor]

  case class StartScript(fileName: String, parameters: List[(String, String)])
}

class RserveActor extends Actor {
  import RserveActor._

  def receive = {
    case StartScript(fileName: String, parameters: List[(String, String)]) =>
  }
}
