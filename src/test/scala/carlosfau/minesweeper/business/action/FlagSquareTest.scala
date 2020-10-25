package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board
import carlosfau.minesweeper.business.model.Board.Flagged
import eu.timepit.refined.auto._
import org.scalatest.funsuite.AnyFunSuite

class FlagSquareTest extends AnyFunSuite  {
  val rows: Board.Size = 3
  val cols: Board.Size = 2

  val boardRepository = new BoardRepositoryStub()

  private val createBoard = CreateBoard(boardRepository)
  private val flagSquare = FlagSquare(boardRepository)

  implicit class BoardOps(board: Board) {
    def flagCount: Int = board.map { (r, c) => board.square(r, c).get}.count{_ == Flagged}
  }


  test("Marking a square it is marked and the only marked") {
    createBoard(rows, cols)

    val board = flagSquare(1, 1).get

    assert(board.square(1, 1).get == Flagged)

    assert( 1 == board.flagCount  )
  }

  test("Marking a marked board (different square) it is marked and rest keeps as original") {
    createBoard(rows, cols)

    flagSquare(1, 1)

    val board = flagSquare(2, 0).get

    assert(board.square(1, 1).get == Flagged)
    assert(board.square(2, 0).get == Flagged)

    assert( 2 == board.flagCount )
  }
}
