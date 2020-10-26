package carlosfau.minesweeper.business.model

import carlosfau.minesweeper.business.model.Board._
import eu.timepit.refined.api.RefinedTypeOps
import eu.timepit.refined.auto._

import scala.annotation.tailrec
import scala.util.Random

/**
 * Board is the instance of a game of Minesweeper
 * @param rows number of rows in th board
 * @param cols number of columns in the board
 */
class Board private (
             val rows: Size, val cols: Size,
             mines: Set[Board.Position],
             cells: Map[Board.Position, SquareView]
           ) {

  // Public for testing
  def withMineAt(row: SquareCoordinate, col: SquareCoordinate): Board =
    copy(mines = mines + (row at col))


  def flagAt(row: SquareCoordinate, col: SquareCoordinate, flagType: Flagged): Result[Board] =
    flagAt(row at col, flagType)

  private def flagAt(position: Position, flagType: Flagged): Result[Board] = {
    cells.get(position) match {
      case Some(Uncovered(_)) => CannotFlagUncoveredSquare(position)
      case Some(`flagType`) => copy(cells = cells - position).asResult
      case _ => uncheckedFlatAt(position, flagType)
    }
  }

  private def uncheckedFlatAt(position: Position, flagType: Flagged) = {
    copy(cells = cells + (position -> flagType)).asResult
  }

  private def copy(cells: Map[Position, SquareView] = cells, mines: Set[Board.Position] = mines) =
    new Board(rows, cols, mines, cells)

  def uncover(row: SquareCoordinate, col: SquareCoordinate): Result[Board] = uncover(row at col)

  private def range(value: SquareCoordinate, limit: Size) = Range(if (value == One) One else value - 1, Math.min(value + 1, limit)).inclusive

  def adjacentMineCount(position: Position): Quantity = Quantity.unsafeFrom((for {
    row <- range(position.row, rows)
    col <- range(position.col, cols)
    if mines contains Position(row, col)
  } yield ()).size)

  private def uncover(position: Position): Result[Board] =
    if (mines contains position) BlewMineUp
    else {
      cells.get(position) match {
        case Some(Uncovered(_)) => CannotUncoverUncoveredSquare(position)
        case _ => uncheckedUncover(position)
      }
    }

  private def uncheckedUncover(position: Position) =
    copy(cells = cells + (position -> Uncovered(adjacentMineCount(position)))).asResult

  def isMineAt(r: SquareCoordinate, c: SquareCoordinate): Boolean = mines contains (r at c)

  /**
   * Returns the cell at coordinates if coordinates are in range, or None if outside board size
   */
  def square(row: SquareCoordinate, col: SquareCoordinate): Option[SquareView] =
    if (row <= rows && col <= cols) Some(unsafeSquare(row, col))
    else None

  /**
   * Internal access to a cell, coordinates are not checked
   */
  private def unsafeSquare(row: SquareCoordinate, col: SquareCoordinate): SquareView =
    cells.getOrElse(row at col, Covered)

  /**
   * Returns all the cells in the board
   * @return a sequence of cells
   */
  def squares: Seq[SquareView] = map(unsafeSquare)

  @tailrec
  private def generateRandomUnusedPosition(mines: Set[Position]): Position = {
    val position = Position(
      SquareCoordinate.unsafeFrom(Random.nextInt(rows) + 1), SquareCoordinate.unsafeFrom(Random.nextInt(cols) + 1)
    )

    if (mines contains position) generateRandomUnusedPosition(mines) else position
  }

  /**
   * Returns a new board identical to this, but wit the given number of mines added
   *
   * @return a new board
   */
  def addMines(numberOfMinesToAdd: Quantity): Board = copy(
    mines = (1 to numberOfMinesToAdd).foldLeft(mines){ (ms, _) => ms + generateRandomUnusedPosition(ms) }
  )

  /**
   * Visit all squares and apply the provided function to each position
   * @param function the function to apply on each square
   * @tparam T  type of the result
   * @return a sequence of the results of applying the function to the squares
   */
  def map[T](function: (SquareCoordinate, SquareCoordinate) => T): Seq[T] = for {
    row <- (1 to rows).asSquareCoordinates
    col <- (1 to cols).asSquareCoordinates
  } yield function(row, col)
}

object Board {
  def apply(rows: Size, cols: Size) = new Board(rows, cols, Set.empty, Map.empty)

  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.numeric._

  type SquareCoordinate = Int Refined Positive
  type Size = Int Refined Positive
  type Quantity = Int Refined NonNegative

  val One: SquareCoordinate = 1

  case class Position(row: SquareCoordinate, col: SquareCoordinate)

  object Position {
    def apply(row: Int, col: Int): Position =
      Position(SquareCoordinate.unsafeFrom(row), SquareCoordinate.unsafeFrom(col))
  }

  object Size extends RefinedTypeOps[Size, Int]
  object SquareCoordinate extends RefinedTypeOps[SquareCoordinate, Int]
  object Quantity extends RefinedTypeOps[Quantity, Int]

  implicit class Range2SquareCoordinates(range: Seq[Int]) {
    def asSquareCoordinates: Seq[SquareCoordinate] = range.map(SquareCoordinate.unsafeFrom)
  }

  sealed abstract class SquareView {
    override def toString: String = getClass.getSimpleName
  }

  object Covered extends SquareView
  abstract class Flagged extends SquareView

  object RedFlagged extends Flagged
  object QuestionMarked extends Flagged

  case class Uncovered(adjacentMines: Quantity) extends SquareView {

    override def toString: String = s"${getClass.getSimpleName}($adjacentMines)"
  }
}
