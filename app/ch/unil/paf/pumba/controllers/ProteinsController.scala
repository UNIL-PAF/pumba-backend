package ch.unil.paf.pumba.controllers

import ch.unil.paf.pumba.common.helpers.DataNotFoundException
import ch.unil.paf.pumba.dataset.models.{DataSetId, Sample}
import ch.unil.paf.pumba.protein.services.{PeptideMatchService, ProteinMergeService, ProteinService}

import javax.inject._
import play.api._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}
import ch.unil.paf.pumba.dataset.models.DataSetJsonFormats.{formatDataBaseName, formatDataSet, formatDataSetId, formatSample}
import ch.unil.paf.pumba.protein.models.ProteinJsonFormats._
import ch.unil.paf.pumba.protein.models.{OrganismName, ProteinId, ProteinMerge, ProteinMergeWithSequence, ProteinOrGene, ProteinWithDataSet, TheoMergedProtein}
import ch.unil.paf.pumba.sequences.models.ProteinSequence
import ch.unil.paf.pumba.sequences.services.SequenceService
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
  */
@Singleton
class ProteinsController @Inject()(implicit ec: ExecutionContext,
                                   cc: ControllerComponents,
                                   config: Configuration,
                                   val reactiveMongoApi: ReactiveMongoApi)
  extends AbstractController(cc) with MongoController with ReactiveMongoComponents {

  // services
  val proteinService = new ProteinService(reactiveMongoApi)
  val sequenceService = new SequenceService(reactiveMongoApi)

  // environment variables
  val rServeHost: String = config.get[String]("Rserve.host")
  val rServePort: Int = config.get[Int]("Rserve.port")

  def findProteinMerge(proteinId: String) = Action.async {
    val sequence = sequenceService.findSequence(ProteinId(proteinId)).map(List(_))
    val proteinIdSeq: Future[ProteinId] = sequence.map(seq => seq.head.proteinId)
    val sampleProteinsMapFuture: Future[Map[Sample, Seq[ProteinWithDataSet]]] = proteinService.getProteinsBySample(ProteinOrGene(proteinId), None)
    val mergesWithSeqs = mergeProtein(sequence, sequence.map(_.head), proteinIdSeq, sampleProteinsMapFuture)

    mergesWithSeqs.map({ merges =>
      Ok(Json.toJson(merges))
    })
  }

  /**
    * Merge proteins from all datasets and create a theoretical merge.
    *
    * @param proteinId
    * @return
    */
  def findProteinMergeByOrganism(proteinId: String, organism: String, dataSetString: Option[String], isoformId: Option[Int]) = Action.async {

    val dataSetIds: Option[Seq[DataSetId]] = dataSetString.map(s => {
      val SAMPLE_SEP = ","
      s.split(SAMPLE_SEP).map(DataSetId(_))
    })

    val sequences: Future[List[ProteinSequence]] = sequenceService.getSequences(ProteinOrGene(proteinId), OrganismName(organism), isoformId: Option[Int])

    val mainSequence: Future[ProteinSequence] = sequences.map(seq => if (seq.length == 1) seq(0) else seq.find(s => s.isoformId.isEmpty).get)
    val proteinIdSeq: Future[ProteinId] = mainSequence.map(seq => seq.proteinId)

    val sampleProteinsMapFuture: Future[Map[Sample, Seq[ProteinWithDataSet]]] = proteinService.getProteinsBySample(ProteinOrGene(proteinId), dataSetIds)

    val mergesWithSeqs = mergeProtein(sequences, mainSequence, proteinIdSeq, sampleProteinsMapFuture)

    mergesWithSeqs.map({ merges =>
      Ok(Json.toJson(merges))
    })

  }

  def mergeProtein(sequences: Future[List[ProteinSequence]],  mainSequence: Future[ProteinSequence], proteinIdSeq: Future[ProteinId], sampleProteinsMapFuture: Future[Map[Sample, Seq[ProteinWithDataSet]]]): Future[ProteinMergeWithSequence] = {

    def mergeProteinsMap(sampleProteinsMap: Future[Map[Sample, Seq[ProteinWithDataSet]]], proteinId: ProteinId, seq: String): Future[(Seq[ProteinMerge], Boolean)] = {
      sampleProteinsMap.flatMap { a =>
        Future.sequence {
          a.map { case (sample: Sample, protList: Seq[ProteinWithDataSet]) =>
            val remapProtList = for {prot <- protList} yield {
              PeptideMatchService().remapPeptides(prot, proteinId, seq)
            }
            Future.fromTry(ProteinMergeService().mergeProteins(remapProtList, sample))
          }.toSeq
        }
      }.map { proteinMerges =>
        val containsNotFirstAC: Boolean = proteinMerges.find(pm => pm.proteins.find(_.isFirstAC.getOrElse(true) == false).isDefined).isDefined
        (proteinMerges, containsNotFirstAC)
      }
    }

    val mergesWithSeqs: Future[ProteinMergeWithSequence] = for {
      seqs <- sequences
      mainSeq <- mainSequence
      protId <- proteinIdSeq
      proteinMerges <- mergeProteinsMap(sampleProteinsMapFuture, protId, mainSeq.sequence)
    } yield {
      val fltSeqs = seqs.filter(_.isoformId.isDefined)
      ProteinMergeWithSequence(proteinMerges._1, fltSeqs, mainSeq, Some(proteinMerges._2))
    }

    mergesWithSeqs
  }

}
