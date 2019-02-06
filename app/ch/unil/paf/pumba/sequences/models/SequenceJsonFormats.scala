package ch.unil.paf.pumba.sequences.models

import ch.unil.paf.pumba.protein.models.OrganismName
import play.api.libs.json._
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._


/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */


object SequenceJsonFormats {

  implicit val formatOrganismName = new Format[OrganismName] {
    override def reads(json: JsValue): JsResult[OrganismName] = JsSuccess(OrganismName(json.as[String]))
    def writes(o: OrganismName) = JsString(o.value)
  }

  implicit val formatDataBaseName = new Format[DataBaseName] {
    override def reads(json: JsValue): JsResult[DataBaseName] = JsSuccess(DataBaseName(json.as[String]))
    def writes(o: DataBaseName) = JsString(o.value)
  }

  implicit val formatProteinSequence = Json.format[ProteinSequence]

}