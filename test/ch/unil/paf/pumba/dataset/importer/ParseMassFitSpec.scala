package ch.unil.paf.pumba.dataset.importer

import java.io.File

import org.specs2.mutable.Specification

/**
  * @author Roman Mylonas
  *         copyright 2018, Protein Analysis Facility UNIL
  */
class ParseMassFitSpec extends Specification{

  "parse coeff file" in {

    val csvFile ="test/resources/dataset/mass_fit_res/mass_fit_coeffs.csv"
    val coeffs: Array[Double] = ParseMassFit().parseCsvCoeffs(csvFile)

    coeffs.length mustEqual 4
    coeffs mustEqual Array(3.001,-0.1028208,0.003104945,-3.993684e-05)
  }

  "parse mass_fits file" in {

    val csvFile ="test/resources/dataset/mass_fit_res/mass_fits.csv"
    val coeffs: Array[Double] = ParseMassFit().parseCsvFits(csvFile)

    coeffs.length mustEqual 57
    coeffs(0) mustEqual 2.901244
    coeffs.last mustEqual -0.1678443
  }

}
