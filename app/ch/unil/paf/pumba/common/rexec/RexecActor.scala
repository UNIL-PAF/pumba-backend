package ch.unil.paf.pumba.common.rexec

import java.io.{File, IOException, PrintWriter}
import java.nio.file.{Files, Path}

import akka.actor._
import ch.unil.paf.pumba.dataset.models._
import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */

object RexecActor {
  def props(changeStatusCallback: ChangeStatusCallback,
            postprocessingCallback: PostprocessingCallback,
            rScriptBin: String,
            stdOutFile: Option[File],
            stdErrFile: Option[File]) = Props(new RexecActor(changeStatusCallback, postprocessingCallback, rScriptBin, stdOutFile, stdErrFile))

  case class StartScript( filePath: Path,
                          parameters: List[String],
                          mockCall: Boolean = false
                        )

  case class ScriptFinished()
}

class RexecActor(changeStatusCallback: ChangeStatusCallback,
                 postprocessingCallback: PostprocessingCallback,
                 rScriptBin: String,
                 stdOutFile: Option[File],
                 stdErrFile: Option[File]) extends Actor with ActorLogging {
  import RexecActor._
  import ch.unil.paf.pumba.common.rexec.RunRScriptActor._
  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Exception => {
        createError(e.getStackTrace.map(_.toString).mkString("\n"))
        Stop
      }
    }

  var runScriptActor: ActorRef = null

  def receive = {

    case StartScript(filePath: Path, parameters: List[String], mockCall: Boolean) => {
      // tell the callback that we started running
      log.info(s"start R script [$filePath]")
      changeStatusCallback.newStatus(DataSetRunning)

      // check if the given R file exists
      if(! Files.exists(filePath)) {
        val errorMessage = s"R script [${filePath.getFileName.toString}] does not exist."
        createError(errorMessage)
      } else {
        // create a new actor that runs the given script
        val command = filePath.toString + " " + parameters.mkString(" ")
        runScriptActor = context.actorOf(RunRScriptActor.props(rScriptBin), "rscript")
        runScriptActor ! RunScript(command, stdOutFile, stdErrFile, mockCall)
        log.info("finished StartScript in RserveActor.")
      }

     }

    case ScriptFinished() => {
      log.info("called ScriptFinished.")
      changeStatusCallback.newStatus(DataSetRunning, message = Some("R script is done. Start post-processing."))
      val futureNrProts = postprocessingCallback.startPostProcessing()

      futureNrProts.onComplete {
        case Success(a) => {
          log.info(s"Finished inserting [$a] proteins.")
          changeStatusCallback.newStatus(DataSetDone, message = Some("Dataset is added to the database."))
        }
        case Failure(e) => {
          createError(e.getStackTrace.map(_.toString).mkString("\n"))
        }
      }


      context.stop(runScriptActor)
      self ! PoisonPill
    }
  }

  private def createError(errorMessage: String) = {
    log.error(errorMessage)
    changeStatusCallback.newStatus(DataSetError, message = Some(errorMessage))
    self ! PoisonPill
  }
}
