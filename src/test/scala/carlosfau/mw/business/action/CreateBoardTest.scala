package carlosfau.mw.business.action

import carlosfau.mw.business.model.Board
import carlosfau.mw.business.model.Board.SquareCoordinate
import carlosfau.mw.business.model.Board.Hidden
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import eu.timepit.refined.api.RefType
import eu.timepit.refined.boolean._
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.generic._
import eu.timepit.refined.string._
import org.scalatest.funsuite.AnyFunSuite

class CreateBoardTest extends AnyFunSuite {

  test("New board has required rows and column squares") {
    val rows: Board.Size = 3
    val cols: Board.Size = 2

    val createBoard = CreateBoard()
    val board = createBoard(rows, cols)

    assert(board.rows == rows)
    assert(board.cols == cols)
  }

  test("New board squares are all hidden") {
    val rows: Board.Size = 3
    val cols: Board.Size = 2

    val createBoard = CreateBoard()
    val board = createBoard(rows, cols)

    assert(board.squares.forall(_ == Hidden))
  }
}
