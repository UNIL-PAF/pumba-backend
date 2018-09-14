package ch.unil.paf.pumba.common.rexec

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */

import java.io.{File, FileOutputStream, PrintWriter}

import akka.actor._
import ch.unil.paf.pumba.common.rexec.RexecActor.ScriptFinished
import ch.unil.paf.pumba.dataset.models.DataSetError

object RunRScriptActor {
  def props(rscriptPath: String) = Props(new RunRScriptActor(rscriptPath))

  case class RunScript(command: String, stdOutFile: Option[File], stdErrFile: Option[File], mockCall : Boolean = false)
}

class RunRScriptActor(rscriptPath: String) extends Actor with ActorLogging {

  import RunRScriptActor._

  def receive = {
    case RunScript(command: String, stdOutFile: Option[File], stdErrFile: Option[File], mockCall: Boolean) =>
      // for the tests
      if(mockCall) mockRunScript(command)
      // and in real life
      else runRScript(command, stdOutFile, stdErrFile)
      sender ! ScriptFinished()
  }

  /**
    ** run R command
    *
    * @param command
    * @param stdOutFile
    * @param stdErrFile
    */
  private def runRScript(command: String, stdOutFile: Option[File], stdErrFile: Option[File]): Unit = {
    log.info(s"run R script with command [$command]")
    if(rscriptPath.isEmpty) throw new RexecException("Path to Rscript is not defined.")
    val execString = s"${rscriptPath} ${command}"
    import sys.process._
    val stdOutWriter: Option[PrintWriter] = stdOutFile.map(new PrintWriter(_))
    val stdErrWriter: Option[PrintWriter] = stdErrFile.map(new PrintWriter(_))

    // actually run the command
    val res: Int =  if(stdOutFile.nonEmpty && stdErrFile.nonEmpty) {
                      execString !(ProcessLogger(stdOutWriter.get.println, stdErrWriter.get.println))
                    } else execString !


    // close the files
    stdOutWriter.map(_.close)
    stdErrWriter.map(_.close)

    if(res != 0) throw new RexecException(s"Command [${execString}] returned error status.")
    log.info("script is done.")
  }

  /**
    * Create or append to log files
    * @param file
    * @return
    */
  private def createPrintWriter(file: Option[File]): Option[PrintWriter] = {
    file.map( f => {
      if(f.exists && ! f.isDirectory){
        new PrintWriter(new FileOutputStream(f, true))
      }else{
        new PrintWriter(f)
      }
    })
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
