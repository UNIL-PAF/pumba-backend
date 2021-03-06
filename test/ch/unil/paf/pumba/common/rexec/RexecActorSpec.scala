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
  *         copyright 2018, Protein Analysis Facility UNIL
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
      // since it's a mock call we don't provide any stdout or stderr files
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "", None, None)), "rserve-1")

      // check if actor stopped
      val testProbe = TestProbe()
      testProbe watch rexecActor

      rexecActor ! StartScript(filePath = rScriptPath, parameters = List(), mockCall = true)
      expectNoMessage(200 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("done")
      testChangeStatusCallback.getLastMessage() should equal ("Dataset is added to the database.")
      postprocessingCallback.isPostprocessingDone() should equal (true)

      testProbe.expectTerminated(rexecActor)
    }

    "throw exception when file does not exist" in {
      // since it's a mock call we don't provide any stdout or stderr files
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "", None, None)), "rserve-2")

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
      // since it's a mock call we don't provide any stdout or stderr files
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "", None, None)), "rserve-3")
      rexecActor ! StartScript(filePath = Paths.get("test/resources/common/r/sayHello.R"), parameters = List())
      expectNoMessage(50 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("error")
      testChangeStatusCallback.getLastMessage().contains("Path to Rscript is not defined") should equal (true)
      postprocessingCallback.isPostprocessingDone() should equal (false)
    }

    "throw exception when rScriptBin is wrong" in {
      // since it's a mock call we don't provide any stdout or stderr files
      val rexecActor = TestActorRef(Props(new RexecActor(testChangeStatusCallback, postprocessingCallback, "/invalid/path/Rscript", None, None)), "rserve-4")
      rexecActor ! StartScript(filePath = Paths.get("test/resources/common/r/sayHello.R"), parameters = List())
      expectNoMessage(50 milli)
      testChangeStatusCallback.getLastStatus().value should equal ("error")
      testChangeStatusCallback.getLastMessage().contains("No such file or directory") should equal (true)
      postprocessingCallback.isPostprocessingDone() should equal (false)
    }


  }

}
