package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.dataset.models.Sample
import ch.unil.paf.pumba.protein.models.{ProteinId, ProteinMerge, ProteinWithDataSet, TheoMergedProtein}

import scala.util.Try
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

/**
  * @author Roman Mylonas
  *         copyright 2018-2021, Protein Analysis Facility UNIL
  */
class ProteinMergeService{

  def mergeProteins(proteins: Seq[ProteinWithDataSet], sample: Sample): Try[ProteinMerge] = {

    val extractedData: Seq[Array[ExtractedInt]] = proteins.map{
      p => extractInts(p.intensities, p.dataSet.massFitResult.get.massFitCoeffs, 100)
    }

    val summedInts: Array[(Double, Double)] = sumSlides(extractedData).reverse.toArray.map(a => (a.molMass, a.int))
    val massAndInts: (Array[Double], Array[Double]) = summedInts.unzip

    Try {
      val interpolator = new LoessInterpolator(0.03, 0)
      val smooth = interpolator.smooth(massAndInts._1, massAndInts._2)

      val mainProtId = proteins(0).proteinIDs(0)
      val mergeName = mainProtId + ":(" + proteins.map(_.dataSet.sample).mkString(";") + ")"
      val mergedProtein: TheoMergedProtein = TheoMergedProtein(mergeName, massAndInts._1, smooth)
      ProteinMerge(mainProtId, sample, mergedProtein, proteins)
    }
  }


  def fittingFunc(x: Double, massFitCoeffs: Seq[Double]): Double = {
    massFitCoeffs(0) + massFitCoeffs(1) * x + massFitCoeffs(2) * Math.pow(x, 2) + massFitCoeffs(3) * Math.pow(x, 3)
  }

  def extractInts(ints: Seq[Double], massFitCoeffs: Seq[Double], cutSize: Int): Array[ExtractedInt] ={

    val limits: Seq[Double] = (0.5 to (ints.length + 0.5) by 1).dropRight(1)

    (0 to (limits.length - 1)).map{ i =>
      val limit: Double = limits(i)
      val cuts: Seq[Double] = (limit to (limit + 1) by (1d/cutSize)).dropRight(1)

      val extractedInts: Seq[ExtractedInt] = cuts.map{ cut =>
        val roundedCut = BigDecimal(cut).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble
        val weight = fittingFunc(roundedCut, massFitCoeffs)
        ExtractedInt(cut = roundedCut, int = ints(i), molMass = weight)
      }
      extractedInts
    }.flatten.toArray

  }


  def sumSlides(extractedData: Seq[Array[ExtractedInt]]): Seq[SumSlide] = {
    val nrSamples = extractedData.length

    // take the first as reference
    val ref = extractedData(0)

    val summedInts: Seq[SumSlide] = (0 to (ref.length - 1)).map{ i =>
      val refMass = ref(i).molMass
      val refInt = ref(i).int

      // define the borders of the current slice
      val distLeft = if(i > 0) ref(i-1).molMass - refMass else refMass - ref(i+1).molMass
      val distRight = if(i < (ref.length - 1)) refMass - ref(i+1).molMass else distLeft
      val limitLeft = refMass - distLeft/2
      val limitRight = refMass + distRight/2

      val intRes: Double = if(nrSamples > 1){
        // look for entries within this border in the other samples
        val sampleInts: Double = (1 to (nrSamples - 1)).map{ idx =>
          val sampleData = extractedData(idx)
          val sampleSlicesOk: Array[ExtractedInt] = sampleData.filter { sd =>
            sd.molMass >= limitLeft && sd.molMass < limitRight
          }

          if(sampleSlicesOk.length > 0) {
            sampleSlicesOk.map(_.int).sum
          }else{
            0
          }
        }.sum

        sampleInts + refInt

      }else{
        refInt
      }

      SumSlide(refMass, intRes/nrSamples)

    }
    summedInts
  }

}

object ProteinMergeService {
  def apply() = new ProteinMergeService()
}

case class ExtractedInt (cut: Double, int: Double, molMass: Double)

case class SumSlide (molMass: Double, int: Double)
