package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board.{Covered, SquareCoordinate}
import carlosfau.minesweeper.business.model.{Board, GameAlreadyStarted}
import carlosfau.minesweeper.infrastructure.repository.InMemoryBoardRepository
import eu.timepit.refined.auto._
import org.scalatest.funsuite.AnyFunSuite

class CreateBoardTest extends AnyFunSuite {
  val rows: Board.Size = 3
  val cols: Board.Size = 2

  private val boardRepository = new InMemoryBoardRepository
  private val createBoard = CreateBoard(boardRepository)

  test("New board has required rows and column squares") {
    val board = createBoard(rows, cols).get

    assert(board.rows == rows)
    assert(board.cols == cols)
  }

  test("New board squares are all hidden") {
    val board = createBoard(rows, cols).get

    assert(board.squares.forall(_ == Covered))
  }

  test("A new board has 0 mines") {
    val board = createBoard(rows, cols).get

    assert( board.map( (r, c) => board.isMineAt(r,c)).forall(!_))
  }

  test("Adding mines to board, then it have the quantity added") {
    val board = createBoard(rows, cols).get.addMines(3).get

    val function: (SquareCoordinate, SquareCoordinate) => Boolean = (r, c) => board.isMineAt(r, c)
    val numberOfMines: Int = board.map(function).count(x => x)
    assert( 3 == numberOfMines )
  }

  test("Cannot add mines to an already started game") {
    createBoard(rows, cols)
    val board = FlagSquare(boardRepository)(1, 1).get

    val result = board.addMines(1).left.get

    assert( result.isInstanceOf[GameAlreadyStarted] )
  }
}
