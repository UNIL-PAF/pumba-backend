package ch.unil.paf.pumba.common.rserve

import java.nio.file.{Files, Path}

import akka.actor._
import ch.unil.paf.pumba.dataset.models._

import scala.concurrent.duration._
import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor.{Actor, ActorLogging, AllForOneStrategy}


/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

object RserveActor {
  def props(changeStatusCallback: ChangeStatusCallback, postprocessingCallback: PostprocessingCallback) = Props(new RserveActor(changeStatusCallback, postprocessingCallback))

  case class StartScript( filePath: Path,
                          parameters: List[(String, String)],
                          mockCall: Boolean = false
                        )

  case class ScriptFinished()

  case class ScriptException(exception: RserveException)
}

class RserveActor(changeStatusCallback: ChangeStatusCallback, postprocessingCallback: PostprocessingCallback) extends Actor with ActorLogging {
  import RserveActor._
  import ch.unil.paf.pumba.common.rserve.RunRScriptActor._

  var runScriptActor: ActorRef = null

  def receive = {

    case StartScript(filePath: Path, parameters: List[(String, String)], mockCall: Boolean) => {
      // tell the callback that we started running
      log.info(s"start R script [$filePath]")
      changeStatusCallback.newStatus(DataSetRunning)

      // check if the given R file exists
      if(! Files.exists(filePath)) {
        val errorMessage = s"R script [${filePath.getFileName.toString}] does not exist."
        createError(errorMessage)
      } else {
        // create a new actor that runs the given script
        val command = filePath.toString
        runScriptActor = context.actorOf(RunRScriptActor.props(), "rscript")
        runScriptActor ! RunScript(command, mockCall)
        log.info("finished StartScript in RserveActor")
      }

    }

    case ScriptFinished => {
      log.info("called ScriptFinished")
      changeStatusCallback.newStatus(DataSetRunning, message = Some("R script is done. Start post-processing."))
      postprocessingCallback.startPostProcessing()
      context.stop(runScriptActor)
      self ! PoisonPill
    }
  }

  private def createError(errorMessage: String) = {
    log.info(errorMessage)
    changeStatusCallback.newStatus(DataSetError, message = Some(errorMessage))
    self ! PoisonPill
    throw new RserveException(message = errorMessage)
  }
}
