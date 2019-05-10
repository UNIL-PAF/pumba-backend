package ch.unil.paf.pumba.dataset.models

import play.api.libs.json._
import ch.unil.paf.pumba.sequences.models.DataBaseName

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */

object DataSetJsonFormats {

  implicit val formatDataSetId = new Format[DataSetId] {
    override def reads(json: JsValue): JsResult[DataSetId] = JsSuccess(DataSetId(json.as[String]))

    def writes(o: DataSetId) = JsString(o.value)
  }

  implicit val formatSample = new Format[Sample] {
    override def reads(json: JsValue): JsResult[Sample] = JsSuccess(Sample(json.as[String]))

    def writes(o: Sample) = JsString(o.value)
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

  implicit val formatDataBaseName = new Format[DataBaseName] {
    override def reads(json: JsValue): JsResult[DataBaseName] = JsSuccess(DataBaseName(json.as[String]))
    def writes(o: DataBaseName) = JsString(o.value)
  }

  implicit val formatMassFitResult = Json.format[MassFitResult]

  implicit val formatDataSet = Json.format[DataSet]

}

