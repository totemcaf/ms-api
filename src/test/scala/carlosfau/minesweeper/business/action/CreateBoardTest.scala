package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board
import carlosfau.minesweeper.business.model.Board.SquareCoordinate
import carlosfau.minesweeper.business.model.Board.Hidden
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
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class CreateBoardTest extends AnyFunSuite {
  val rows: Board.Size = 3
  val cols: Board.Size = 2

  private val createBoard = CreateBoard(new BoardRepositoryStub)

  test("New board has required rows and column squares") {
    val board = createBoard(rows, cols)

    assert(board.rows == rows)
    assert(board.cols == cols)
  }

  test("New board squares are all hidden") {
    val board = createBoard(rows, cols)

    assert(board.squares.forall(_ == Hidden))
  }

  test("A new board has 0 mines") {
    val board = createBoard(rows, cols)

    assert( board.map( (r, c) => board.mineAt(r,c)).forall(!_))
  }

  test("Adding mines to board, then it have the quantity added") {
    val board = createBoard(rows, cols).addMines(3)

    val function: (SquareCoordinate, SquareCoordinate) => Boolean = (r, c) => board.mineAt(r, c)
    val numberOfMines: Int = board.map(function).count(x => x)
    assert( 3 == numberOfMines )
  }
}
