package ch.unil.paf.pumba.common.rexec

/**
  * @author Roman Mylonas
  *  copyright 2018, Protein Analysis Facility UNIL
  */
final case class RexecException(private val message: String = "",
                                private val cause: Throwable = None.orNull) extends Exception(message, cause)

