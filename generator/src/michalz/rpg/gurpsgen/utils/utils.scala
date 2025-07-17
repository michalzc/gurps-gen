package michalz.rpg.gurpsgen.utils

import scala.util.Failure
import scala.util.Success
import scala.util.Try

extension [T](self: Try[T])
  def orThrow: T = self match {
    case Success(value) => value
    case Failure(ex)    => throw ex
  }

extension [E, T](self: Either[E, T])
  def orThrow: T = self match {
    case Left(ex: Throwable) => throw ex
    case Left(other)  => throw new Exception(other.toString)
    case Right(value) => value
  }
