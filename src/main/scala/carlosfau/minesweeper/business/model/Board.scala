package carlosfau.minesweeper.business.model

import carlosfau.minesweeper.business.model.Board._
import eu.timepit.refined.api.RefinedTypeOps
import eu.timepit.refined.auto._

import scala.util.Random

/**
 * Board is the instance of a game of Minesweeper
 * @param rows number of rows in th board
 * @param cols number of columns in the board
 */
case class Board(rows: Size, cols: Size, mines: Set[Board.Position] = Set.empty) {

  def mineAt(r: SquareCoordinate, c: SquareCoordinate): Boolean = mines contains (r,c)

  /**
   * Returns the cell at coordinates if coordinates are in range, or None if outside board size
   */
  def square(row: SquareCoordinate, col: SquareCoordinate): Option[SquareView] =
    if (row < rows && col < cols) Some(unsafeSquare(row, col))
    else None

  /**
   * Internal access to a cell, coordinates are not checked
   */
  private def unsafeSquare(row: SquareCoordinate, col: SquareCoordinate) = Hidden

  /**
   * Returns all the cells in the board
   * @return a sequence of cells
   */
  def squares: Seq[SquareView] = map(unsafeSquare)

  private def generateRandomUnusedPosition(mines: Set[Position]): Position = {
    val position: Position = (
      SquareCoordinate.unsafeFrom(Random.nextInt(rows)), SquareCoordinate.unsafeFrom(Random.nextInt(cols))
    )

    if (mines contains position) generateRandomUnusedPosition(mines) else position
  }

  /**
   * Returns a new board identical to this, but wit the given number of mines added
   *
   * @return a new board
   */
  def addMines(numberOfMinesToAdd: Quantity): Board = copy(
    mines = (1 to numberOfMinesToAdd).foldLeft(mines){ (ms, _) => ms + generateRandomUnusedPosition(mines) }
  )

  /**
   * Visit all squares and apply the provided function to each position
   * @param function  the function to apply on each square
   * @tparam T  type of the result
   * @return a sequence of the results of applying the function to the squares
   */
  def map[T](function: (SquareCoordinate, SquareCoordinate) => T): Seq[T] = for {
    row <- (0 until rows).asSquareCoordinates
    col <- (0 until cols).asSquareCoordinates
  } yield function(row, col)
}

object Board {
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.numeric._

  type SquareCoordinate = Int Refined NonNegative
  type Size = Int Refined Positive
  type Quantity = Int Refined NonNegative

  type Position = (SquareCoordinate, SquareCoordinate)

  object Size extends RefinedTypeOps[Size, Int]
  object SquareCoordinate extends RefinedTypeOps[SquareCoordinate, Int]
  object Quantity extends RefinedTypeOps[Quantity, Int]

  implicit class Range2SquareCoordinates(range: Seq[Int]) {
    def asSquareCoordinates: Seq[SquareCoordinate] = range.map(SquareCoordinate.unsafeFrom)
  }

  sealed abstract class SquareView
  object Hidden extends SquareView
}
