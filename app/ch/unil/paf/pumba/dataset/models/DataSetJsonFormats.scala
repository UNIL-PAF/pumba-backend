package ch.unil.paf.pumba.dataset.models

import play.api.libs.json._

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

object DataSetJsonFormats {

  implicit val formatDataSetId = new Format[DataSetId] {
    override def reads(json: JsValue): JsResult[DataSetId] = JsSuccess(DataSetId(json.as[String]))

    def writes(o: DataSetId) = JsString(o.value)
  }

  implicit val formatDataSetStatus = new Format[DataSetStatus] {
    override def reads(json: JsValue): JsResult[DataSetStatus] = {
      JsSuccess(json.as[String] match {
        case "created" => DataSetCreated
        case "running" => DataSetRunning
        case "error" => DataSetError
        case "done" => DataSetDone
      })
    }

    def writes(o: DataSetStatus) = JsString(o.value)
  }

  implicit val formatDataSet = Json.format[DataSet]

}

