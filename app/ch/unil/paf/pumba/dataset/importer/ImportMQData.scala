package ch.unil.paf.pumba.dataset.importer

import java.io.File

import ch.unil.paf.pumba.common.helpers.{FileFinder, Unzip}
import ch.unil.paf.pumba.dataset.models.DataSetId

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
class ImportMQData {

  /**
    * Unpack the zip file and give back the path to the txt directory
    * @param zipFile
    * @param removeZip
    * @return
    */
  def unpackZip(zipFile: File, destPath: File, removeZip: Boolean): File = {
    val isValidDir = Unzip.unzip(zipFile, destPath)

    if(! isValidDir) throw new Exception("ZIP file is empty [" + zipFile.getName + "]")

    val files: List[File] = FileFinder.getListOfDirs(destPath.getAbsolutePath)

    // check if there is a txt dir within the zip
    val txtDirOption = files.find(file => file.isDirectory && file.getName == "txt")

    val txtDir = txtDirOption match {
      case Some(file) => file
      case None => throw new Exception("Could not find any txt directory in [" + zipFile.getName + "]")
    }

    // remove the zip file
    if(removeZip) zipFile.delete

    return txtDir
  }


  /**
    * Create a new actor which makes the call to Rserve
    *
    * @param proteinGroups
    * @return returns
    */
  def computeMassFit(proteinGroups: File): DataSetId = {
    return DataSetId("dummy")
  }


}


object ImportMQData {
  def apply() = new ImportMQData()
}