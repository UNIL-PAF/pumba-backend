package ch.unil.paf.pumba.common.rserve

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
class DummyPostproccessingCallback extends PostprocessingCallback {
  override def startPostProcessing(): Unit = {
    println("start postprocessing")
  }
}
