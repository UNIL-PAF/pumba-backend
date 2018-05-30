package ch.unil.paf.pumba

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
trait PlayWithControllerSpec extends PlayWithMongoSpec{

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
}
