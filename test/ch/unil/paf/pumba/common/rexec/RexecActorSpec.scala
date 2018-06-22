package ch.unil.paf.pumba.common.rexec

import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import ch.unil.paf.pumba.common.rexec.RexecActor.StartScript
import ch.unil.paf.pumba.dataset.models.DataSetId
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
class RexecActorSpec extends TestKit(ActorSystem("RserveActorSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "RexecActor" must {

    // create a changeStatusCallback for this test
    val testChangeStatusCallback = new TestChangeStatusCallback(id = DataSetId("test-set-id"))
    val postprocessingCallback = new TestPostprocessingCallback
    val rScriptPath = Paths.get("test/resources/common/r/sayHello.R")

    "change status" in {
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "")), "rserve-1")

      // check if actor stopped
      val testProbe = TestProbe()
      testProbe watch rexecActor

      rexecActor ! StartScript(filePath = rScriptPath, parameters = List(), mockCall = true)
      expectNoMessage(200 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("running")
      testChangeStatusCallback.getLastMessage() should equal ("R script is done. Start post-processing.")
      postprocessingCallback.isPostprocessingDone() should equal (true)

      testProbe.expectTerminated(rexecActor)
    }

    "throw exception when file does not exist" in {
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "")), "rserve-2")

      // check if actor stopped
      val testProbe = TestProbe()
      testProbe watch rexecActor

      rexecActor ! StartScript(filePath = Paths.get("not_existant.R"), parameters = List())
      expectNoMessage(50 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("error")
      testChangeStatusCallback.getLastMessage() should equal ("R script [not_existant.R] does not exist.")
      postprocessingCallback.isPostprocessingDone() should equal (false)

      testProbe.expectTerminated(rexecActor)
    }

    "throw exception when rScriptBin does not exist" in {
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "")), "rserve-3")
      rexecActor ! StartScript(filePath = Paths.get("test/resources/common/r/sayHello.R"), parameters = List())
      expectNoMessage(50 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("error")
      testChangeStatusCallback.getLastMessage() should equal ("Path to Rscript is not defined.")
      postprocessingCallback.isPostprocessingDone() should equal (false)
    }

    "throw exception when rScriptBin is wrong" in {
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "/invalid/path/Rscript")), "rserve-4")
      rexecActor ! StartScript(filePath = Paths.get("test/resources/common/r/sayHello.R"), parameters = List())
      expectNoMessage(50 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("error")
      testChangeStatusCallback.getLastMessage() should equal ("Cannot run program \"/invalid/path/Rscript\": error=2, No such file or directory")
      postprocessingCallback.isPostprocessingDone() should equal (false)
    }


  }

}
