package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.common.helpers.{DataNotFoundException, DatabaseException}
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.protein.models._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers.await
import reactivemongo.api.commands.WriteResult
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test.Helpers._
import org.scalatest._
import Matchers._
import ch.unil.paf.pumba.dataset.services.DataSetService

import scala.concurrent.Future

/**
  * @author Roman Mylonas
  * copyright 2018-2020, Protein Analysis Facility UNIL
  */
class ProteinServiceSpec extends PlayWithMongoSpec with BeforeAndAfter {

  val proteinService = new ProteinService(reactiveMongoApi)
  val dataSetService = new DataSetService(reactiveMongoApi)

  val protein = Protein(
    dataSetId = DataSetId("dummy_id"),
    proteinIDs = Seq("A0A096LP75", "C4AMC7", "Q6VEQ5", "Q9NQA3", "A8K0Z3").map(ProteinId(_)),
    geneNames = Seq("WASH3P", "WASH2P", "WASH6P", "WASH1").map(GeneName(_)),
    theoMolWeight = 50.073,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31049000, 108350000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    peptides = Seq.empty[Peptide]
  )

  val protein_2 = Protein(
    dataSetId = DataSetId("dummy_id"),
    proteinIDs = Seq("A0A096LPI6", "P30042", "A0A096LP75").map(ProteinId(_)),
    geneNames = Seq("C21orf33").map(GeneName(_)),
    theoMolWeight = 30.376,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0),
    peptides = Seq.empty[Peptide]
  )

  val protein_3 = Protein(
    dataSetId = DataSetId("dummy_id_2"),
    proteinIDs = Seq("A0A096LPI6", "P30042", "A0A096LP75").map(ProteinId(_)),
    geneNames = Seq("C21orf33").map(GeneName(_)),
    theoMolWeight = 30.376,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0),
    peptides = Seq.empty[Peptide]
  )


  val protein_4 = Protein(
    dataSetId = DataSetId("dummy_id_3"),
    proteinIDs = Seq("A0A096LPI6", "P30042", "A0A096LP75").map(ProteinId(_)),
    geneNames = Seq("C21orf33").map(GeneName(_)),
    theoMolWeight = 30.376,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0),
    peptides = Seq.empty[Peptide]
  )

  val protein_delete_me = Protein(
    dataSetId = DataSetId("delete_me"),
    proteinIDs = Seq("P02786").map(ProteinId(_)),
    geneNames = Seq("TFRC").map(GeneName(_)),
    theoMolWeight = 30.376,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111630000, 32980000, 0, 0, 0, 0, 0, 0, 0, 0),
    peptides = Seq.empty[Peptide]
  )

  val dataSet_1 = DataSet(
    id = DataSetId("dummy_id"),
    name = "dummy",
    sample = Sample("Jurkat"),
    status = DataSetDone,
    message = None,
    massFitResult = None,
    dataBaseName = None,
    colorGroup = 1
  )

  val dataSet_2 = DataSet(
    id = DataSetId("dummy_id_2"),
    name = "dummy 2",
    sample = Sample("Jurkat II"),
    status = DataSetDone,
    message = None,
    massFitResult = None,
    dataBaseName = None,
    colorGroup = 1
  )

  val dataSet_3 = DataSet(
    id = DataSetId("dummy_id_3"),
    name = "dummy 3",
    sample = Sample("Blublu"),
    status = DataSetDone,
    message = None,
    massFitResult = None,
    dataBaseName = None,
    colorGroup = 1
  )

  val dataSet_4 = DataSet(
    id = DataSetId("dummy_id_4"),
    name = "dummy 4",
    sample = Sample("Blublu"),
    status = DataSetDone,
    message = None,
    massFitResult = None,
    dataBaseName = None,
    colorGroup = 1
  )

  before {
    //Init DB
    await(proteinService.insertProtein(protein))
    await(proteinService.insertProtein(protein_2))
    await(proteinService.insertProtein(protein_3))
    await(proteinService.insertProtein(protein_4))
    await(proteinService.insertProtein(protein_delete_me))

    await(dataSetService.insertDataSet(dataSet_1))
    await(dataSetService.insertDataSet(dataSet_2))
    await(dataSetService.insertDataSet(dataSet_3))
    await(dataSetService.insertDataSet(dataSet_4))
  }

  after {
    //clean DB
    await(proteinService.dropAll())
  }

  "ProteinService" should {

    val protein_3 = protein_2.copy(dataSetId = DataSetId("dummy_id_2"))

    "insert a protein" in {
      val res: WriteResult = await(proteinService.insertProtein(protein_3))
      res.ok mustEqual (true)
    }

    "find a protein from a dataset" in {
      val res: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("dummy_id"), ProteinOrGene("A0A096LPI6")))
      res.length mustEqual 1
      res(0).theoMolWeight mustEqual 30.376
    }

    "find another protein from a dataset" in {
      val res: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("dummy_id"), ProteinOrGene("C4AMC7")))
      res.length mustEqual 1
      res(0).theoMolWeight mustEqual 50.073
    }

    "find multiple proteins from a dataset" in {
      val res: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("dummy_id"), ProteinOrGene("A0A096LP75")))
      res.length mustEqual 2
      res.filter(_.theoMolWeight == 30.376).length mustEqual 1
    }

    "throw exception when not finding protein" in {
      val res: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("not_existing"), ProteinOrGene("not_existing")))
      res.length mustEqual 0


//      ScalaFutures.whenReady(res.failed) { e =>
//        e shouldBe a [DataNotFoundException]
//      }
    }

    "find protein" in {
      val res: List[Protein] = await(proteinService.getProteins(ProteinId("A0A096LP75")))
      res.length mustEqual 4
      res.filter(_.theoMolWeight == 30.376).length mustEqual 3
    }

    "find protein with dataSet" in {
      val res: Map[Sample, Seq[ProteinWithDataSet]] = await(proteinService.getProteinsBySample(ProteinOrGene("A0A096LP75"), None))
      res.keys.toSeq.length mustEqual 3
      res.contains(Sample("Jurkat II")) mustEqual true
      res(Sample("Jurkat II")).length mustEqual 7
    }

    "find protein with dataSet for certain dataSets" in {
      val dataSets = Seq(DataSetId("dummy_id"), DataSetId("dummy_id_3"), DataSetId("dummy_id_4"))
      val res: Map[Sample, Seq[ProteinWithDataSet]] = await(proteinService.getProteinsBySample(ProteinOrGene("A0A096LP75"), dataSetIds = Some(dataSets)))

      res.keys.toSeq.length mustEqual 3
    }

    "find samples from given protein" in {
      val res:Map[Sample, Seq[DataSetId]] = await(proteinService.getSamplesFromProtein(ProteinOrGene("A0A096LP75")))
      res.keys.toSeq.length mustEqual 3
      res.contains(Sample("Jurkat II")) mustEqual true
      res(Sample("Jurkat II")).length mustEqual 9
    }

  }

  "filterSampleDataSetMap" should {

    val sampleDataSetMap = Future { Map(Sample("sample 1") -> List(DataSetId("set 1_1"), DataSetId("set 1_2")), Sample("sample 2") -> List(DataSetId("set 2_1"), DataSetId("set 2_2"), DataSetId("set 2_3"))) }

    "filter the map" in {
      val dataSetIds = Some(List(DataSetId("set 1_1"), DataSetId("set 1_3"), DataSetId("set 2_2")))
      val fltRes = await(proteinService.filterSampleDataSetMap(sampleDataSetMap, dataSetIds))
      fltRes.keySet mustEqual(Set(Sample("sample 1"), Sample("sample 2")))
    }

    "filter the map with empty dataSetIds" in {
      val fltRes = await(proteinService.filterSampleDataSetMap(sampleDataSetMap, None))
      fltRes mustEqual await(sampleDataSetMap)
    }

  }


  "deleteProtein" should {
    "delete all proteins with dataSetId delete_me" in {
      val beforeDelete: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("delete_me"), ProteinOrGene("P02786")))
      beforeDelete.length mustEqual 1

      val deleteRes: WriteResult = await(proteinService.removeProteins(DataSetId("delete_me")))
      deleteRes.ok mustEqual true

      val afterDelete: List[Protein] = await(proteinService.getProteinsFromDataSet(DataSetId("delete_me"), ProteinOrGene("P02786")))
      afterDelete.length mustEqual 0
    }
  }

}
