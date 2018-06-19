package ch.unil.paf.pumba.common.rexec

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

import akka.actor._
import ch.unil.paf.pumba.common.rexec.RexecActor.ScriptFinished

object RunRScriptActor {
  def props() = Props[RunRScriptActor]

  case class RunScript(command: String, mockCall: Boolean = false)
}

class RunRScriptActor extends Actor with ActorLogging {

  import RunRScriptActor._

  def receive = {
    case RunScript(command: String, mockCall: Boolean) =>
      if(mockCall) mockRserve(command)
      else runRScript(command)
      sender ! ScriptFinished()
  }

  /**
    * run R command
    *
    * @param command
    */
  private def runRScript(command: String): Unit = {
    log.info(s"run real script with command [$command]")
    val sleepTime = 100
    log.info(s"sleep for $sleepTime millis")
    Thread.sleep(sleepTime)
    log.info("finished mock script")
  }


  /**
    * make a mock call to Rserve for testing
    *
    * @param command
    */
  private def mockRserve(command: String): Unit = {
    log.info(s"run mock script with command [$command]")
    val sleepTime = 100
    log.info(s"sleep for $sleepTime millis")
    Thread.sleep(sleepTime)
    log.info("finished mock script")
  }

}
