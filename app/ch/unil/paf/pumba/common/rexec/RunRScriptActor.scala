package ch.unil.paf.pumba.common.rexec

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

import akka.actor._
import ch.unil.paf.pumba.common.rexec.RexecActor.ScriptFinished

object RunRScriptActor {
  def props(rscriptPath: String) = Props(new RunRScriptActor(rscriptPath))

  case class RunScript(command: String, mockCall: Boolean = false)
}

class RunRScriptActor(rscriptPath: String) extends Actor with ActorLogging {

  import RunRScriptActor._

  def receive = {
    case RunScript(command: String, mockCall: Boolean) =>
      // for the tests
      if(mockCall) mockRunScript(command)
      // and in real life
      else runRScript(command)
      sender ! ScriptFinished()
  }

  /**
    * run R command
    *
    * @param command
    */
  private def runRScript(command: String): Unit = {
    log.info(s"run R script with command [$command]")
    if(rscriptPath.isEmpty) throw new RexecException("Path to Rscript is not defined.")
    val execString = s"${rscriptPath} ${command}"
    import sys.process._
    val res: Int = execString !;
    if(res != 0) throw new RexecException(s"Command [${execString}] returned error status.")
    log.info("script is done.")
  }


  /**
    * make a mock call to Rserve for testing
    *
    * @param command
    */
  private def mockRunScript(command: String): Unit = {
    log.info(s"run mock script with command [$command]")
    val sleepTime = 100
    log.info(s"sleep for $sleepTime millis")
    Thread.sleep(sleepTime)
    log.info("finished mock script")
  }

}
