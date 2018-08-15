package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models._
import play.api.libs.json._
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats.formatDataSetId

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */

object ProteinJsonFormats {

  implicit val formatProtein = Json.format[Protein]

}