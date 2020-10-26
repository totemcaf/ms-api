package carlosfau.minesweeper.business

import carlosfau.minesweeper.business.model.Board.{Position, SquareCoordinate}

package object model {
  type Result[T] = Either[GameError, Option[T]]

  implicit class ResultOps[T](result: Result[T]) {
    def map2[V](f: T => V): Result[V] = result.map(_.map{f(_)})
    def flatMap2[V](f: T => Result[V]): Result[V] = result.flatMap{
      case Some(t) => f(t)
      case None => Right(None)
    }

    def get: T = result.toOption.get.get
  }

  implicit def error2result[T](error: GameError): Result[T] = Left(error)

  sealed abstract class GameError() {
    val msg: String


    override def toString = s"${getClass.getSimpleName}($msg)"
  }

  case class InvalidAction(msg: String) extends GameError
  object BlewMineUp extends GameError {
    val msg = "Game over, you uncover a mine"
  }

  implicit class IntToPosition(row: SquareCoordinate) {
    def at(col: SquareCoordinate): Position = Position(row, col)
  }
}
