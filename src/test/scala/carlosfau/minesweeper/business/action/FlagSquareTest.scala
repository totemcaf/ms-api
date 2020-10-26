package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board
import carlosfau.minesweeper.business.model.Board.{Covered, Flagged}
import eu.timepit.refined.auto._
import org.scalatest.funsuite.AnyFunSuite

class FlagSquareTest extends AnyFunSuite  {
  val rows: Board.Size = 3
  val cols: Board.Size = 2

  val boardRepository = new BoardRepositoryStub()

  private val createBoard = CreateBoard(boardRepository)
  private val flagSquare = FlagSquare(boardRepository)

  implicit class BoardOps(board: Board) {
    def flagCount: Int = {
      val value = board.map { (r, c) => board.square(r, c).get }
      value.count {
        _ == Flagged
      }
    }
  }


  test("Flagging a square it is flagged and it is the only flagged") {
    createBoard(rows, cols)

    val board = flagSquare(1, 1).get

    assert(board.square(1, 1).get == Flagged)

    assert( 1 == board.flagCount  )
  }

  test("Flagging a flagged board (different square) it is flagged and rest keeps as original") {
    createBoard(rows, cols)

    flagSquare(2, 2)

    val board = flagSquare(3, 1).get

    assert(board.square(2, 2).get == Flagged)
    assert(board.square(3, 1).get == Flagged)

    assert( 2 == board.flagCount )
  }

  test("Flagging a flagged square, unflagged it") {
    createBoard(rows, cols)

    flagSquare(2, 1)

    val board = flagSquare(2, 1).get

    assert(board.square(2, 1).get == Covered)

    assert( 0 == board.flagCount )
  }

  // Flagging an uncovered square generates an error
}
