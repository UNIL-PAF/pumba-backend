package ch.unil.paf.pumba.common.helpers

/**
  * @author Roman Mylonas
  *         copyright 2016-2017, SIB Swiss Institute of Bioinformatics
  */
final case class DatabaseException(private val message: String = "",
                                 private val cause: Throwable = None.orNull) extends Exception(message, cause)


