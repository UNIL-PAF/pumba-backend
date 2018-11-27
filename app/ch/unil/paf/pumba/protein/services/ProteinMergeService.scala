package ch.unil.paf.pumba.protein.services

import java.util.Calendar

import ch.unil.paf.pumba.protein.models.{ProteinWithDataSet, TheoMergedProtein}
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

  val loadLibRes: REXP = rConnection.eval("library(pumbaR)")

  var multiplier = 2

  Logger.info(loadLibRes.toDebugString)

  def multiplyThis(d: Int): Int = {

    val name = "list_" + Calendar.getInstance().getTimeInMillis.toString

    val rListBuff = new StringBuilder
    rListBuff.append(s"$name <- list();\n")
    rListBuff.append(s"$name <- list();\n")

    println(rListBuff.toString)

    val res: Int = rConnection.eval(s"Sys.sleep(5); $d*$multiplier;").asInteger()
    multiplier += 1
    Logger.info(res.toString)
    res
  }

  def mergeProteins(proteins: Seq[ProteinWithDataSet]): TheoMergedProtein = {
    // make a new unique name for the list
    val name = Calendar.getInstance().getTimeInMillis.toString

    val rListBuff = new StringBuilder
    rListBuff.append(s"$name <- list();\n")

    println(rListBuff.toString)

    ???
  }

}

/**
  * companion object - we want only one Rserve instance and pumbaR loaded
  */
object ProteinMergeService {

  def apply(rServeHost: String, rServePort: Int):ProteinMergeService = {
    new ProteinMergeService(rServeHost, rServePort)
  }

}
