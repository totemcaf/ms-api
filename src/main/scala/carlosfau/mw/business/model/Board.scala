package carlosfau.mw.business.model

import carlosfau.mw.business.model.Board._
import eu.timepit.refined.api.RefinedTypeOps
import eu.timepit.refined.auto._

case class Board(rows: Size, cols: Size) {
  /**
   * Currently it is unsafe, no upper limits checked
   */
  def square(row: SquareCoordinate, col: SquareCoordinate): Option[SquareView] =
    if (row < rows && col < cols) Some(unsafeSquare(row, col))
    else None

  private def unsafeSquare(row: SquareCoordinate, col: SquareCoordinate) = Hidden

  def squares = for {
  row <- (0 until rows).asSquareCoordinates
  col <- (0 until cols).asSquareCoordinates
  } yield unsafeSquare(row, col)
}

object Board {
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.numeric._

  type SquareCoordinate = Int Refined NonNegative
  type Size = Int Refined Positive

  object Size extends RefinedTypeOps[Size, Int]
  object SquareCoordinate extends RefinedTypeOps[SquareCoordinate, Int]

  implicit class Range2SquareCoordinates(range: Seq[Int]) {
    def asSquareCoordinates = range.map(SquareCoordinate.unsafeFrom)
  }

  sealed abstract class SquareView
  object Hidden extends SquareView
}
