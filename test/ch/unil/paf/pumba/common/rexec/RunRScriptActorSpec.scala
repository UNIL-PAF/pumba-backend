package ch.unil.paf.pumba.common.rexec

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import ch.unil.paf.pumba.common.rexec.RexecActor.ScriptFinished
import ch.unil.paf.pumba.common.rexec.RunRScriptActor.RunScript
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class RunRScriptActorSpec extends TestKit(ActorSystem("RserveActorSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "RunRScriptActor" must {

    "work in mock mode" in {
      val runRScriptActor = TestActorRef(Props(new RunRScriptActor("")), "rscript-1")
      runRScriptActor ! RunScript(command = "some_fake_command", mockCall = true)
      expectMsgType[ScriptFinished]
    }

  }

}
