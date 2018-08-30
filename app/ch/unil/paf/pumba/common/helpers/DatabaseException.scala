package ch.unil.paf.pumba.common.helpers

import play.api.Logger

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Roman Mylonas
  * copyright 2018, Protein Analysis Facility UNIL
  */


final case class DatabaseException(private val message: String = "",
                                 private val cause: Throwable = None.orNull) extends Exception(message, cause)

trait DatabaseError {
  /**
    * generic function which checks if the result is valid and otherwise throws a DataBaseExcpetion
    * @param res
    * @param check
    * @param error
    * @tparam A
    */
  def checkOrError[A](res: Future[A], check: A => Boolean, error: A => String): Future[A] = {
    res.transform {
      case Success(res) => if (check(res)) Failure(new DatabaseException(error(res))) else Success(res)
      case Failure(t) => {
        Logger.error("An error occured in DataSetService: " + t.toString)
        Failure(new DatabaseException(t.getMessage))
      }
    }
  }
}


