package ch.unil.paf.pumba.common.helpers

import play.api.Logger

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */


case class DatabaseException(private val message: String = "",
                                 private val cause: Throwable = None.orNull) extends Exception(message, cause)

case class DataNotFoundException(private val message: String = "",
                             private val cause: Throwable = None.orNull) extends Exception(message, cause)



trait DatabaseError {
  /**
    * generic function which checks if the result is valid and otherwise throws a DatabaseException
    * @param res
    * @param check
    * @param error
    * @tparam A
    */
  def checkOrError[A](res: Future[A], check: A => Boolean, error: A => String, dataNotFound: Boolean = false): Future[A] = {
    res.transform {
      case Success(res) => if (check(res)) Failure(createException(error(res), dataNotFound)) else Success(res)
      case Failure(t) => {
        Logger.error("An error occurred in DataSetService: " + t.toString)
        Failure(createException(t.getMessage, dataNotFound))
      }
    }
  }

  def createException(message: String, dataNotFound: Boolean): Exception = {
    if(dataNotFound) DatabaseException(message)
    else DataNotFoundException(message)
  }

}


