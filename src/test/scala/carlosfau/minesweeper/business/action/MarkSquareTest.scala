package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board
import carlosfau.minesweeper.business.model.Board.Marked
import eu.timepit.refined.auto._
import org.scalatest.funsuite.AnyFunSuite

class MarkSquareTest extends AnyFunSuite  {
  val rows: Board.Size = 3
  val cols: Board.Size = 2

  val boardRepository = new BoardRepositoryStub()

  private val createBoard = CreateBoard(boardRepository)
  private val markSquare = MarkSquare(boardRepository)

  implicit class BoardOps(board: Board) {
    def markCount: Int = board.map { (r, c) => board.square(r, c).get}.count{_ == Marked}
  }


  test("Marking a square it is marked and the only marked") {
    createBoard(rows, cols)

    val board = markSquare(1, 1).get

    assert(board.square(1, 1).get == Marked)

    assert( 1 == board.markCount  )
  }

  test("Marking a marked board (different square) it is marked and rest keeps as original") {
    createBoard(rows, cols)

    markSquare(1, 1)

    val board = markSquare(2, 0).get

    assert(board.square(1, 1).get == Marked)
    assert(board.square(2, 0).get == Marked)

    assert( 2 == board.markCount )
  }
}
