package ch.unil.paf.pumba.protein.services

import java.util.Calendar

import ch.unil.paf.pumba.common.rexec.RexecException
import ch.unil.paf.pumba.dataset.models.Sample
import ch.unil.paf.pumba.protein.models.{ProteinMerge, ProteinWithDataSet, TheoMergedProtein}
import org.rosuda.REngine._
import org.rosuda.REngine.Rserve.{RConnection, RserveException}
import play.api.Logger

import scala.util.{Failure, Success, Try}


/**
  * @author Roman Mylonas
  *         copyright 2018-2020, Protein Analysis Facility UNIL
  */
class ProteinMergeServiceR(rServeHost: String, rServePort: Int){

  // make the RConnection
  val rConnection = new RConnection(rServeHost, rServePort)

  // load pumbaR
  val loadLibRes: REXP = rConnection.eval("library(pumbaR)")

  def mergeProteins(proteins: Seq[ProteinWithDataSet], sample: Sample): Try[ProteinMerge] = {
    // make a new unique name for the list
    val uniqTag = Calendar.getInstance().getTimeInMillis.toString
    val listName = "list_" + uniqTag
    val resName = "res_" + uniqTag

    val rCommandBuff = new StringBuilder
    rCommandBuff.append(s"$listName <- list();\n")

    // build up the list with data for the merge
    for((prot, i) <- proteins.zip(Stream from 1)){
      val mass_fit_params = prot.dataSet.massFitResult.get.massFitCoeffs.mkString(",")
      val ints = prot.intensities.mkString(",")
      rCommandBuff.append(s"$listName[[$i]] <- list();\n")
      rCommandBuff.append(s"$listName[[$i]][['mass_fit_params']] <- c($mass_fit_params);\n")
      rCommandBuff.append(s"$listName[[$i]][['ints']] <- c($ints);\n")
    }

    // the merge function
    rCommandBuff.append(s"$resName <- merge_proteins($listName, cut_size=100, loess_span=0.05);\n")
    rCommandBuff.append(s"$resName")
    val rCommand = rCommandBuff.toString

    Logger.info("Start R command: [" + rCommand + "].")

    val resObj: Try[REXP] = Try(rConnection.eval(rCommand))
    val resTry: Try[RList] = resObj.map(_.asList)

    val protMerge: Try[ProteinMerge] = resTry.flatMap { res =>
      // remove all R objects
      val removeTry: Try[REXP] = Try(rConnection.eval(s"rm($listName, $resName)"))

      removeTry.map( remove => {
        Logger.info("R command finished, clean session.")

        // create the result
        val mainProtId = proteins(0).proteinIDs(0)
        val mergeName = mainProtId + ":(" + proteins.map(_.dataSet.sample).mkString(";") + ")"
        val mergedProtein: TheoMergedProtein = new TheoMergedProtein(mergeName, res.at("x").asDoubles, res.at("y").asDoubles)
        new ProteinMerge(mainProtId, sample, mergedProtein, proteins)
      })

    }

    // we want the command in case it was an RserveException
    protMerge match {
      case Failure(e: RserveException) => Failure(RexecException(s"Failed to execute command: [$rCommand]", e))
      case Failure(e) => Failure(e)
      case Success(s) => Success(s)
    }

  }

}

/**
  * companion object - we want only one Rserve instance and pumbaR loaded
  */
object ProteinMergeServiceR {

  //var proteinMergeService: ProteinMergeServiceR = null

  def apply(rServeHost: String, rServePort: Int):ProteinMergeServiceR = {
//    if(proteinMergeService == null){
//      proteinMergeService = new ProteinMergeServiceR(rServeHost, rServePort)
//    }
//    proteinMergeService
    new ProteinMergeServiceR(rServeHost, rServePort)
  }

}
