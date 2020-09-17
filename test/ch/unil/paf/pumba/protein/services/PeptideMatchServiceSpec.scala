package ch.unil.paf.pumba.protein.services

import ch.unil.paf.pumba.PlayWithMongoSpec
import ch.unil.paf.pumba.dataset.models._
import ch.unil.paf.pumba.dataset.services.DataSetService
import ch.unil.paf.pumba.protein.models._
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfter, _}
import org.specs2.mutable.Specification
import play.api.test.Helpers.{await, _}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Roman Mylonas
  * copyright 2018-2020, Protein Analysis Facility UNIL
  */
class PeptideMatchServiceSpec extends Specification {

  val peptides = Seq(
    Peptide(
      proteinIDs = Seq("A0A096LP75").map(ProteinId(_)),
      maxQuantId = MaxQuantPepId(3296),
      sequence = "ATLLESIR",
      aminoAcidBefore = Some("R"),
      aminoAcidAfter = Some("Q"),
      startPos = Some(362),
      endPos = Some(369),
      isRazor = Some(true),
      sliceNr = 24,
      theoMass = 2.9549769459724504,
      score = 116.73,
      uniqueByGroup = true,
      intensity = 27317000
    ),

    Peptide(
      proteinIDs = Seq("C4AMC7", "Q6VEQ5").map(ProteinId(_)),
      maxQuantId = MaxQuantPepId(13580),
      sequence = "GPGAGEGPGGAFAR",
      aminoAcidBefore = Some("K"),
      aminoAcidAfter = Some("V"),
      startPos = Some(426),
      endPos = Some(439),
      isRazor = Some(true),
      sliceNr = 24,
      theoMass = 3.0790249804978087,
      score = 122.46,
      uniqueByGroup = true,
      intensity = 14560000
    ),

    Peptide(
      proteinIDs = Seq("Q6VEQ5").map(ProteinId(_)),
      maxQuantId = MaxQuantPepId(13580),
      sequence = "GPGAGEGPGGAFAR",
      aminoAcidBefore = Some("K"),
      aminoAcidAfter = Some("V"),
      startPos = Some(426),
      endPos = Some(439),
      isRazor = Some(true),
      sliceNr = 25,
      theoMass = 3.0790249804978087,
      score = 122.46,
      uniqueByGroup = true,
      intensity = 50000
    )
  )

  val massFitRes_1 = MassFitResult(
    massFitCoeffs = Array(0),
    massFitPicturePath = "",
    proteinGroupsPath = "",
    massFitRData = "",
    peptidesPath = "",
    massFits = Array(0),
    maxInt = 0,
    normCorrFactor = 10
  )

  val dataSet_1 = DataSet(
    id = DataSetId("dummy_id"),
    name = "dummy",
    sample = Sample("Jurkat"),
    status = DataSetDone,
    message = None,
    massFitResult = Some(massFitRes_1),
    dataBaseName = None,
    colorGroup = 1,
    organism = "human"
  )

  val protein: ProteinWithDataSet = ProteinWithDataSet(
    proteinIDs = Seq("A0A096LP75", "C4AMC7", "Q6VEQ5", "Q9NQA3", "A8K0Z3").map(ProteinId(_)),
    geneNames = Seq("WASH3P", "WASH2P", "WASH6P", "WASH1").map(GeneName(_)),
    theoMolWeight = 50.073,
    intensities = Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31049000, 108350000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    dataSet = dataSet_1,
    peptides = peptides
  )



  "PeptideMatchService" should {

    "correctly remap A0A096LP75" in {
      val mappedPeps = PeptideMatchService().remapPeptides(protein, ProteinId("A0A096LP75"), "MTPVRMQHSLAGQTYAVPLIQPDLRREEAVQQMADALQYLQKVSGDIFSRISQQVEQSRSQVQAIGEKVSLAQAKIEKIKGSKKAIKVFSSAKYPAPGRLQEYGSIFTGAQDPGLQRRPHRIQSKHRPLDERALQEKLKDFPVCVSTKPEPEDDAEEGLGGLPSNISSVSSLLLFNTTENLYKKYVFLDPLAGAVTKTHVMLGAETEEKLFDAPLSISKREQLEQQVPENYFYVPDLGQVPEIDVPSYLPDLSGIANDLMYIADLGPGIAPSAPGTIPELPTFHTEVAEPLKVDLQDGVLTPPPPPPPPPPAPEVLASAPPLPPSTAAPVGQGARQDDSSSSASPSVQGAPREVVDPSGGRATLLESIRQAGGIGKAKLRSMKERKLEKKKQKEQEQVRATSQGGHLMSDLFNKLVMRRKGISGKGPGAGEGPGGAFARVSDSIPPLPPPQQPQAEEDEDDWES")
      mappedPeps.peptides.length mustEqual(1)
      mappedPeps.peptides(0).startPos mustEqual (Some(362))
      mappedPeps.peptides(0).endPos mustEqual (Some(369))
      mappedPeps.peptides(0).aminoAcidBefore mustEqual (Some("R"))
      mappedPeps.peptides(0).aminoAcidAfter mustEqual (Some("Q"))
      mappedPeps.intensities.sum mustEqual(1.39399E8)
    }

    "correctly remap C4AMC7" in {
      val mappedPeps = PeptideMatchService().remapPeptides(protein, ProteinId("C4AMC7"), "MTPVRMQHSLAGQTYAVPLIQPDLRREEAVQQMADALQYLQKVSGDIFSRISQQVEQSRSQVQAIGEKVSLAQAKIEKIKGSKKAIKVFSSAKYPAPERLQEYGSIFTGAQDPGLQRRPRHRIQSKHRPLDERALQEKDFPVCVSTKPEPEDDAEEGLGGLPSNISSVSSLLLFNTTENLGKKYVFLDPLAGAVTKTHVMLGAETEEKLFDAPLSISKREQLEQQVPENYFYVPDLGQVPEIDVPSYLPDLPGITNDLMYIADLGPGIAPSAPGTIPELPTFHTEVAEPLKVDLQDGVLTPPPPPPPPPPAPEVLASAPPLPPSTAAPVGQGARQDDSSSSASPSVQGAPREVVDPSGGRATLLESIRQAGGIGKAKLRSMKERKLEKKQQKEQEQVRATSQGGHLMSDLFNKLVMRRKGISGKGPGAGEGPGGAFARVSDSIPPLPPPQQPQAEEDEDDWES")
      mappedPeps.peptides.length mustEqual(1)
      mappedPeps.peptides(0).startPos mustEqual (Some(426))
      mappedPeps.peptides(0).endPos mustEqual (Some(439))
      mappedPeps.peptides(0).aminoAcidBefore mustEqual (Some("K"))
      mappedPeps.peptides(0).aminoAcidAfter mustEqual (Some("V"))
      mappedPeps.intensities.sum mustEqual(1456000.0)
    }

    "correctly remap Q6VEQ5" in {
      val mappedPeps = PeptideMatchService().remapPeptides(protein, ProteinId("Q6VEQ5"), "MTPVRMQHSLAGQTYAVPLIQPDLRREEAVQQMADALQYLQKVSGDIFSRISQQVEQSRSQVQAIGEKVSLAQAKIEKIKGSKKAIKVFSSAKYPAPERLQEYGSIFTGAQDPGLQRRPRHRIQSKHRPLDERALQEKLKDFPVCVSTKPEPEDDAEEGLGGLPSNISSVSSLLLFNTTENLYKKYVFLDPLAGAVTKTHVMLGAETEEKLFDAPLSISKREQLEQQVPENYFYVPDLGQVPEIDVPSYLPDLPGIANDLMYIADLGPGIAPSAPGTIPELPTFHTEVAEPLKVDLQDGVLTPPPPPPPPPPAPEVLASAPPLPPSTAAPVGQGARQDDSSSSASPSVQGAPREVVDPSGGRATLLESIRQAGGIGKAKLRSMKERKLEKKKQKEQEQVRATSQGGHLMSDLFNKLVMRRKGISGKGPGAGEGPGGAFARVSDSIPPLPPPQQPQAEEDEDDWES")
      mappedPeps.peptides.length mustEqual(2)
      mappedPeps.peptides(0).startPos mustEqual (Some(427))
      mappedPeps.peptides(0).endPos mustEqual (Some(440))
      mappedPeps.peptides(0).aminoAcidBefore mustEqual (Some("K"))
      mappedPeps.peptides(0).aminoAcidAfter mustEqual (Some("V"))
      mappedPeps.intensities.sum mustEqual(1461000.0)
    }

  }
}
