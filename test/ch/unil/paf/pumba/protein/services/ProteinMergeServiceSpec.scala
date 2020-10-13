package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.protein.models._
import org.specs2.mutable.Specification

/**
  * @author Roman Mylonas
  * copyright 2018-2020, Protein Analysis Facility UNIL
  */
class ProteinMergeServiceSpec extends Specification {

  val massFitParams: Seq[Double] = Seq(2.90230588635708,-0.0990513683718742,0.00309367801794186,-4.05829976900451e-05)
  val ints: Seq[Double] = Seq(1930800,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,3145700,4036800,6036000,6696500000d,
    8679700000d,63454000,0,0,0,0,0,0,0,0,0)

  val massFitParams1: Seq[Double] = Seq(2.92541101792906,-0.0856835305957989,
    0.00229499350478187,-2.85888515674011e-05)
  val ints1: Seq[Double] = Seq(1930800,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,3145700,4036800,6036000,6696500000d,
    8679700000d,63454000,0,0,0,0,0,0,0,0,0)

  val massFitParams2: Seq[Double] = Seq(2.93157752998003,-0.0911120501100777,
    0.00261614574546816,-3.38080473782906e-05)
  val ints2: Seq[Double] = Seq(0,0,0,424060,0,0,26561000,0,0,0,8257400,0,0,0,0,0,0,
    0,0,4896700,3668500,0,0,0,0,52009000,0,0,0,0,0,0,
    5325700,2393800,2973300,4061200000d,3175600000d,
    35474000,25662000,18546000,0,0,7599400,0,0,5936300,0)

  val massFitParams3: Seq[Double] = Seq(2.86255232915833,-0.0860407095427795,
    0.00249869975354482,-3.31808222647113e-05)
  val ints3: Seq[Double] = Seq(1053800,0,0,0,0,0,0,0,3159900,1061300,0,4672100,0,0,
    2268100,0,0,0,0,0,0,0,0,0,3289300,0,0,0,5302800,
    4653300,0,7113600,24130000,6285400,460980000,
    1.3405e+10,3747900000d,17037000,15226000,0,0,
    17088000,3872700,0,14897000,8817200)


  val extData: Seq[Array[ExtractedInt]] = Seq(ProteinMergeService().extractInts(ints1, massFitParams1, 100),
    ProteinMergeService().extractInts(ints2, massFitParams2, 100),
    ProteinMergeService().extractInts(ints3, massFitParams3, 100)
  )

  val extInts = ProteinMergeService().extractInts(ints, massFitParams, 100)

  val summedSlides: Seq[SumSlide] = ProteinMergeService().sumSlides(extData)

  "extractInts" should {

    // round the molMass
    val roundedExtInts = extInts.map( e => ExtractedInt(e.cut, e.int, BigDecimal(e.molMass).setScale(6, BigDecimal.RoundingMode.HALF_UP).toDouble))

    "have correct length" in {
      roundedExtInts.length mustEqual(4700)
    }

    "have correct entries" in {
      roundedExtInts(0) mustEqual(ExtractedInt(0.5, 1930800, 2.853549))
      roundedExtInts(99) mustEqual(ExtractedInt(1.49, 1930800, 2.761453))
      roundedExtInts(999) mustEqual(ExtractedInt(10.49, 0, 2.15684))
      roundedExtInts(4699) mustEqual(ExtractedInt(47.49, 0, 0.828919))
    }
  }


  "sumSlides" should {

    // round the molMass
    val roundedSummedSlides = summedSlides.map( s => SumSlide(BigDecimal(s.molMass).setScale(6, BigDecimal.RoundingMode.HALF_UP).toDouble, Math.round(s.int)))

    "have correct length" in {
      roundedSummedSlides.length mustEqual(4700)
    }

    "have correct entries" in {
      roundedSummedSlides(0) mustEqual (SumSlide(2.883139, 643600))
      roundedSummedSlides(99) mustEqual (SumSlide(2.802743, 994867))
      roundedSummedSlides(999) mustEqual (SumSlide(2.246131, 1053300))
      roundedSummedSlides(4699) mustEqual (SumSlide(0.970213, 4917833))
    }

  }

}
