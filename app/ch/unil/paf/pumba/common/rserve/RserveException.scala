package ch.unil.paf.pumba.common.rserve

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
final case class RserveException(private val message: String = "",
                                 private val cause: Throwable = None.orNull) extends Exception(message, cause)

