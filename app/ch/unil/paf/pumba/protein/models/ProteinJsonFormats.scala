package ch.unil.paf.pumba.protein.models

import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats.{formatDataSet, formatDataSetId, formatSample, formatDataBaseName}
import ch.unil.paf.pumba.sequences.models.ProteinSequence
import play.api.libs.json._



/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */

object ProteinJsonFormats {

  implicit val formatOrganismName = new Format[OrganismName] {
    override def reads(json: JsValue): JsResult[OrganismName] = JsSuccess(OrganismName(json.as[String]))
    def writes(o: OrganismName) = JsString(o.value)
  }

  implicit val formatMaxQuantPepId = new Format[MaxQuantPepId] {
    override def reads(json: JsValue): JsResult[MaxQuantPepId] = JsSuccess(MaxQuantPepId(json.as[Int]))
    def writes(o: MaxQuantPepId) = JsNumber(o.value)
  }

  implicit val formatProteinId = new Format[ProteinId] {
    override def reads(json: JsValue): JsResult[ProteinId] = JsSuccess(ProteinId(json.as[String]))
    def writes(o: ProteinId) = JsString(o.value)
  }

  implicit val formatGeneName= new Format[GeneName] {
    override def reads(json: JsValue): JsResult[GeneName] = JsSuccess(GeneName(json.as[String]))
    def writes(o: GeneName) = JsString(o.value)
  }

  implicit val formatProteinOrGene= new Format[ProteinOrGene] {
    override def reads(json: JsValue): JsResult[ProteinOrGene] = JsSuccess(ProteinOrGene(json.as[String]))
    def writes(o: ProteinOrGene) = JsString(o.value)
  }

  implicit val formatProteinEntryName= new Format[ProteinEntryName] {
    override def reads(json: JsValue): JsResult[ProteinEntryName] = JsSuccess(ProteinEntryName(json.as[String]))
    def writes(o: ProteinEntryName) = JsString(o.value)
  }

  implicit val formatProteinName= new Format[ProteinName] {
    override def reads(json: JsValue): JsResult[ProteinName] = JsSuccess(ProteinName(json.as[String]))
    def writes(o: ProteinName) = JsString(o.value)
  }

  implicit val formatPeptide = Json.format[Peptide]

  implicit val formatProtein = Json.format[Protein]

  implicit val formatProteinWithDataSet = Json.format[ProteinWithDataSet]

  implicit val formatTheoMergedProtein = Json.format[TheoMergedProtein]

  implicit val formatProteinMerge = Json.format[ProteinMerge]

  implicit val formatProteinSequence = Json.format[ProteinSequence]

}