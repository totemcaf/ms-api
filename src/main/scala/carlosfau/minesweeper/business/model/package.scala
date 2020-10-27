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

  implicit class Any2Result[T](value: T){
    val asResult: Result[T] = Right(Some(value))
  }

  sealed abstract class GameError() {
    val msg: String

    private def name = getClass.getSimpleName.stripSuffix("$")
    override def toString = s"$name($msg)"
  }


  case class InvalidAction(msg: String) extends GameError
  case class CannotFlagUncoveredSquare(position: Position) extends GameError {
    val msg = s"Cannot flag uncovered square at $position"
  }

  case class CannotUncoverUncoveredSquare(position: Position) extends GameError {
    val msg = s"Cannot uncover already uncovered square at $position"
  }

  object BlewMineUp extends GameError {
    val msg = "Game over, you uncover a mine"
  }

  object EndedGame extends GameError {
    val msg = "The game is already ended, cannot play"
  }
  implicit class IntToPosition(row: SquareCoordinate) {
    def at(col: SquareCoordinate): Position = Position(row, col)
  }
}
