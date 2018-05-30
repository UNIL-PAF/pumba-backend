package ch.unil.paf.pumba

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.ReactiveMongoApi

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

trait PlayWithMongoSpec extends PlaySpec with GuiceOneAppPerSuite {

  override def fakeApplication = new GuiceApplicationBuilder()
    .configure(
      "mongodb.uri" -> "mongodb://localhost:27017/pumba-test"
    )
    .build()

  lazy val reactiveMongoApi = app.injector.instanceOf[ReactiveMongoApi]

}
