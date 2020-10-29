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
    val id = boardRepository.save(Board(rows, cols)).get.id

    val board = uncoverSquare(id,2, 1).get

    assert(board.square(2, 1).get == Uncovered(0))
  }

  test("Uncover covered square with 1 adjacent mine shows it square uncovered with value 1") {
    val id = boardRepository.save(Board(rows, cols).withMineAt(1,1)).get.id

    val board = uncoverSquare(id,2, 1).get

    assert(board.square(2, 1).get == Uncovered(1))
  }

  test("Uncover covered square with 2 adjacent mine shows it square uncovered with value 2") {
    val id = boardRepository.save(
      Board(rows, cols)
        .withMineAt(1,1)
        .withMineAt(3,2)
    ).get.id

    val board = uncoverSquare(id,2, 1).get

    assert(board.square(2, 1).get == Uncovered(2))
  }

  test("Uncover covered square with all adjacent mines shows it square uncovered with value 8") {
    val id = boardRepository.save(
      Board(rows, cols)
        .withMineAt(1,1)
        .withMineAt(1,2)
        .withMineAt(1,3)
        .withMineAt(2,1)
        .withMineAt(2,3)
        .withMineAt(3,1)
        .withMineAt(3,2)
        .withMineAt(3,3)
    ).get.id

    val board = uncoverSquare(id,2, 2).get

    assert(board.square(2, 2).get == Uncovered(8))
  }

  test("Uncover mined square make user to loose") {
    val id = boardRepository.save(Board(rows, cols).withMineAt(2,2)).get.id

    val board = uncoverSquare(id,2, 2).get

    assert(board.state == Lost)
  }

  test("Uncover mined square shows all mines positions") {
    val id = boardRepository
      .save(Board(2, 2).withMineAt(2,2).withMineAt(1,1).withMineAt(1,2))
      .get.id

    val board = uncoverSquare(id,2, 2).get

    assert(board.square(1, 1).get == Mine)
    assert(board.square(2, 1).get == Covered)
    assert(board.square(1, 2).get == Mine)
    assert(board.square(2, 2).get == ExplodedMine)
  }

  test("Uncover mined square shows all incorrect flagged mines positions") {
    val id = boardRepository.save(Board(2, 2).withMineAt(2,2).withMineAt(1,2)).get.id

    flagSquare(id, 1, 1)

    val board = uncoverSquare(id,2, 2).get

    assert(board.square(1, 1).get == IncorrectMine)
  }

  test("Uncovering an uncovered square generates an error") {
    val id = boardRepository.save(Board(rows, cols).withMineAt(1, 1).uncover(2, 3).get).get.id

    val result = uncoverSquare(id,2, 3).swap.getOrElse(fail("Expecting exception"))

    assert(result == CannotUncoverUncoveredSquare(Position(2, 3)))
  }

  test("Uncovering the last cell without mine, the user win"){
    val id = boardRepository.save(Board(1, 1)).get.id

    val board = uncoverSquare(id,1, 1).get

    assert(board.state == Won)
  }

  test("Uncover ended game reports an error") {
    val id = boardRepository.save(Board(1, 1)).get.id
    val board = uncoverSquare(id,1, 1).get
    assert(board.isEnded)

    val result = uncoverSquare(id,1, 1).left.get

    assert(result == EndedGame)
  }


  test("Invalid row position is reported") {
    val id = boardRepository.save(Board(1, 2)).get.id

    val result = uncoverSquare(id,2, 1).left.get

    assert(result.isInstanceOf[InvalidCoordinates])
  }

  test("Invalid col position is reported") {
    val id = boardRepository.save(Board(1, 2)).get.id

    val result = uncoverSquare(id,1, 3).left.get

    assert(result.isInstanceOf[InvalidCoordinates])
  }
}
