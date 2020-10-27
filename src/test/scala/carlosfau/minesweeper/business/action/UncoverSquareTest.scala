package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board.{Covered, ExplodedMine, IncorrectMine, Lost, Mine, Position, Uncovered, Won}
import carlosfau.minesweeper.business.model.{Board, CannotUncoverUncoveredSquare, EndedGame, InvalidCoordinates}
import carlosfau.minesweeper.infrastructure.repository.InMemoryBoardRepository
import eu.timepit.refined.auto._
import org.scalatest.funsuite.AnyFunSuite

class UncoverSquareTest extends AnyFunSuite {
  val rows: Board.Size = 3
  val cols: Board.Size = 3

  val boardRepository = new InMemoryBoardRepository()

  private val flagSquare = FlagSquare(boardRepository)
  private val uncoverSquare = UncoverSquare(boardRepository)

  test("Uncover covered square shows it square uncovered") {
    boardRepository.save(Board(rows, cols))

    val board = uncoverSquare(2, 1).get

    assert(board.square(2, 1).get == Uncovered(0))
  }

  test("Uncover covered square with 1 adjacent mine shows it square uncovered with value 1") {
    boardRepository.save(Board(rows, cols).withMineAt(1,1))

    val board = uncoverSquare(2, 1).get

    assert(board.square(2, 1).get == Uncovered(1))
  }

  test("Uncover covered square with 2 adjacent mine shows it square uncovered with value 2") {
    boardRepository.save(
      Board(rows, cols)
        .withMineAt(1,1)
        .withMineAt(3,2)
    )

    val board = uncoverSquare(2, 1).get

    assert(board.square(2, 1).get == Uncovered(2))
  }

  test("Uncover covered square with all adjacent mines shows it square uncovered with value 8") {
    boardRepository.save(
      Board(rows, cols)
        .withMineAt(1,1)
        .withMineAt(1,2)
        .withMineAt(1,3)
        .withMineAt(2,1)
        .withMineAt(2,3)
        .withMineAt(3,1)
        .withMineAt(3,2)
        .withMineAt(3,3)
    )

    val board = uncoverSquare(2, 2).get

    assert(board.square(2, 2).get == Uncovered(8))
  }

  test("Uncover mined square make user to loose") {
    boardRepository.save(Board(rows, cols).withMineAt(2,2))

    val board = uncoverSquare(2, 2).get

    assert(board.state == Lost)
  }

  test("Uncover mined square shows all mines positions") {
    boardRepository.save(Board(2, 2).withMineAt(2,2).withMineAt(1,1).withMineAt(1,2))

    val board = uncoverSquare(2, 2).get

    assert(board.square(1, 1).get == Mine)
    assert(board.square(2, 1).get == Covered)
    assert(board.square(1, 2).get == Mine)
    assert(board.square(2, 2).get == ExplodedMine)
  }

  test("Uncover mined square shows all incorrect flagged mines positions") {
    boardRepository.save(Board(2, 2).withMineAt(2,2).withMineAt(1,2))

    flagSquare(1, 1)

    val board = uncoverSquare(2, 2).get

    assert(board.square(1, 1).get == IncorrectMine)
  }

  test("Uncovering an uncovered square generates an error") {
    boardRepository.save(Board(rows, cols).uncover(2, 3).get)

    val result = uncoverSquare(2, 3).swap.getOrElse(fail("Expecting exception"))

    assert(result == CannotUncoverUncoveredSquare(Position(2, 3)))
  }

  test("Uncovering the last cell without mine, the user win"){
    boardRepository.save(Board(1, 1))

    val board = uncoverSquare(1, 1).get

    assert(board.state == Won)
  }

  test("Uncover ended game reports an error") {
    boardRepository.save(Board(1, 1))
    val board = uncoverSquare(1, 1).get
    assert(board.isEnded)

    val result = uncoverSquare(1, 1).left.get

    assert(result == EndedGame)
  }


  test("Invalid row position is reported") {
    boardRepository.save(Board(1, 2))

    val result = uncoverSquare(2, 1).left.get

    assert(result.isInstanceOf[InvalidCoordinates])
  }

  test("Invalid col position is reported") {
    boardRepository.save(Board(1, 2))

    val result = uncoverSquare(1, 3).left.get

    assert(result.isInstanceOf[InvalidCoordinates])
  }
}
