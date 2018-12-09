package ch.unil.paf.pumba.protein.services

import java.util.Calendar

import ch.unil.paf.pumba.dataset.models.Sample
import ch.unil.paf.pumba.protein.models.{ProteinMerge, ProteinWithDataSet, TheoMergedProtein}
import org.rosuda.REngine._
import org.rosuda.REngine.Rserve.RConnection
import play.api.Logger

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ProteinMergeService (rServeHost: String, rServePort: Int){

  // make the RConnection
  val rConnection = new RConnection(rServeHost, rServePort)

  // load pumbaR
  val loadLibRes: REXP = rConnection.eval("library(pumbaR)")
  Logger.info(loadLibRes.toDebugString)


  def mergeProteins(proteins: Seq[ProteinWithDataSet], sample: Sample): ProteinMerge = {
    // make a new unique name for the list
    val uniqTag = Calendar.getInstance().getTimeInMillis.toString
    val listName = "list_" + uniqTag

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
    rCommandBuff.append(s"merge_proteins($listName, cut_size=100, loess_span=0.05);\n")
    val rCommand = rCommandBuff.toString
    val resObj = rConnection.eval(rCommand)
    val res:RList = resObj.asList

    // create the result
    val mainProtId = proteins(0).proteinIDs(0)
    val mergeName = mainProtId + ":(" + proteins.map(_.dataSet.sample).mkString(";") + ")"
    val mergedProtein: TheoMergedProtein = new TheoMergedProtein(mergeName, res.at("x").asDoubles, res.at("y").asDoubles)
    val proteinMerge: ProteinMerge = new ProteinMerge(mainProtId, sample, mergedProtein, proteins)
    proteinMerge
  }

}

/**
  * companion object - we want only one Rserve instance and pumbaR loaded
  */
object ProteinMergeService {

  var proteinMergeService: ProteinMergeService = null

  def apply(rServeHost: String, rServePort: Int):ProteinMergeService = {
    if(proteinMergeService == null){
      proteinMergeService = new ProteinMergeService(rServeHost, rServePort)
    }
    proteinMergeService
  }

}
