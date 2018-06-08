package ch.unil.paf.pumba.common.rserve

import java.io.File
import java.nio.file.{Path, Paths}

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import ch.unil.paf.pumba.common.rserve.RserveActor.{ScriptException, ScriptFinished, StartScript}
import ch.unil.paf.pumba.dataset.models.DataSetId
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
class RserveActorSpec extends TestKit(ActorSystem("RserveActorSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "RserveActor" must {

    // create a changeStatusCallback for this test
    val testChangeStatusCallback = new TestChangeStatusCallback(id = DataSetId("test-set-id"))
    val postprocessingCallback = new TestPostprocessingCallback
    val rScriptPath = Paths.get("test/resources/common/r/sayHello.R")

    "change status" in {
      val rserveActor = TestActorRef(Props(new RserveActor(testChangeStatusCallback, postprocessingCallback)), "rserve-1")
      rserveActor ! StartScript(filePath = rScriptPath, parameters = List(), mockCall = true)
      expectNoMessage(200 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("running")
      testChangeStatusCallback.getLastMessage() should equal ("R script is done. Start post-processing.")
      postprocessingCallback.isPostprocessingDone() should equal (true)
    }

    "throw exception when file does not exist" in {
      val rserveActor = TestActorRef(Props(new RserveActor(testChangeStatusCallback, postprocessingCallback)), "rserve-2")
      rserveActor ! StartScript(filePath = Paths.get("not_existant.R"), parameters = List())
      expectNoMessage(150 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("error")
      testChangeStatusCallback.getLastMessage() should equal ("R script [not_existant.R] does not exist.")
      postprocessingCallback.isPostprocessingDone() should equal (false)
    }


//    "stop RunRScriptActor" in {
//
//      val testProbe = TestProbe()
//
//      // start Rserve and wait till script is finished
//      val rserveActor = TestActorRef(Props(new RserveActor(testChangeStatusCallback)), "rserve-3")
//      rserveActor ! StartScript(filePath = "some_R_file", parameters = List())
//
//      // look for an rScriptActor
//      val rScriptActorSelect = testProbe.system.actorSelection("user/rserve-2/rscript")
//      rScriptActorSelect ! PoisonPill
//      val rScriptActor: ActorRef = await(rScriptActorSelect.resolveOne(50 milli))
//
//      // wait for the script to be finished
//      expectMsgType[ScriptFinished]
//
//      // check if rScriptActor died
//      testProbe.expectTerminated(rScriptActor)
//    }

  }

}
