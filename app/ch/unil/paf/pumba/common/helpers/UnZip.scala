package ch.unil.paf.pumba.common.helpers

import java.io.{File, IOException}
import net.lingala.zip4j.core.ZipFile

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL  & Vital-IT Swiss Institute of Bioinformatics
  */
object Unzip {

  /**
    * create a temporary directory and return the absolute path as a String
    *
    * @return absolute path
    */
  def createTmpDir: File = {
    val tmpDir = File.createTempFile("temp", "zip")

    if(!(tmpDir.delete()))
    {
      throw new IOException("Could not delete temp file: " + tmpDir.getAbsolutePath());
    }

    if(!(tmpDir.mkdir()))
    {
      throw new IOException("Could not create temp directory: " + tmpDir.getAbsolutePath());
    }

    return tmpDir
  }

  /**
    * extract the given ZIP file into the given directory
    *
    * @param zipFile
    * @return a boolean indicating if anything got extracted
    */
  def unzip(zipFile: File, extractionDir: File): Boolean = {

    val zip:ZipFile  = new ZipFile(zipFile)
    zip.extractAll(extractionDir.getAbsolutePath)

    (extractionDir.isDirectory && extractionDir.getTotalSpace > 0)
  }

  /**
    * create a tmp dir and extract the zip content inside
    *
    * @param zipFile
    * @return path to extraction directory
    */
  def unzipIntoTmp(zipFile: File): String = {
    val tmpDir = createTmpDir
    unzip(zipFile, tmpDir)
    tmpDir.getAbsolutePath
  }

}
