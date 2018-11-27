package ch.unil.paf.pumba.dataset.importer

import java.io.File

import org.specs2.mutable.Specification

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ParseMassFitCoeffsSpec extends Specification{

  "parse file" in {

    val csvFile ="test/resources/dataset/mass_fit_res/mass_fit_coeffs.csv"
    val coeffs: Array[Double] = ParseMassFitCoeffs().parseCsvFile(csvFile)

    coeffs.length mustEqual 4
    coeffs mustEqual Array(3.001,-0.1028208,0.003104945,-3.993684e-05)

  }

}
