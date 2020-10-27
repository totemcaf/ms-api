package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board.{Covered, Flagged, Position, QuestionMarked, RedFlagged}
import carlosfau.minesweeper.business.model.{Board, CannotFlagUncoveredSquare, EndedGame, InvalidCoordinates}
import carlosfau.minesweeper.infrastructure.repository.InMemoryBoardRepository
import eu.timepit.refined.auto._
import org.scalatest.funsuite.AnyFunSuite

class FlagSquareTest extends AnyFunSuite  {
  val rows: Board.Size = 3
  val cols: Board.Size = 2

  val boardRepository = new InMemoryBoardRepository()

  private val flagSquare = FlagSquare(boardRepository)
  private val uncoverSquare = UncoverSquare(boardRepository)

  implicit class BoardOps(board: Board) {
    def flagCount: Int = {
      val value = board.map { (r, c) => board.square(r, c).get }
      value.count {
        _.isInstanceOf[Flagged]
      }
    }
  }


  test("Flagging a square it is flagged and it is the only flagged") {
    val id = boardRepository.save(Board(rows, cols)).get.id

    val board = flagSquare(id, 1, 1).get

    assert(board.square(1, 1).get == RedFlagged)

    assert( 1 == board.flagCount  )
  }

  test("Flagging a flagged board (different square) it is flagged and rest keeps as original") {
    val id = boardRepository.save(Board(rows, cols)).get.id

    flagSquare(id, 2, 2)

    val board = flagSquare(id, 3, 1).get

    assert(board.square(2, 2).get == RedFlagged)
    assert(board.square(3, 1).get == RedFlagged)

    assert( 2 == board.flagCount )
  }

  test("Flagging a flagged square, unflagged it") {
    val id = boardRepository.save(Board(rows, cols)).get.id

    flagSquare(id, 2, 1)

    val board = flagSquare(id, 2, 1).get

    assert(board.square(2, 1).get == Covered)

    assert( 0 == board.flagCount )
  }

  test("Flagging an uncovered square generates an error") {
    val id = boardRepository.save(Board(rows, cols).uncover(3, 2).get).get.id

    val result = flagSquare(id, 3, 2).swap.getOrElse(fail("Expecting exception"))

    assert(result == CannotFlagUncoveredSquare(Position(3, 2)))
  }

  test("Flagging a square with question mark it is flagged with question mark") {
    val id = boardRepository.save(Board(rows, cols)).get.id

    val board = flagSquare(id, 1, 1, flagType = QuestionMarked).get

    assert(board.square(1, 1).get == QuestionMarked)

    assert( 1 == board.flagCount )
  }

  test("Flag ended game reports an error") {
    val id = boardRepository.save(Board(1, 2).withMineAt(1, 1)).get.id
    val board = uncoverSquare(id, 1, 2).get

    assert(board.isEnded)

    val result = flagSquare(id, 1, 2).left.get

    assert(result == EndedGame)
  }

  test("Invalid row position is reported") {
    val id = boardRepository.save(Board(1, 2)).get.id

    val result = flagSquare(id, 2, 1).left.get

    assert(result.isInstanceOf[InvalidCoordinates])
  }

  test("Invalid col position is reported") {
    val id = boardRepository.save(Board(1, 2)).get.id

    val result = flagSquare(id, 1, 3).left.get

    assert(result.isInstanceOf[InvalidCoordinates])
  }
}
