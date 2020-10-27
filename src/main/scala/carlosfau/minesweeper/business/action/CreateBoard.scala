package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, Result}
import eu.timepit.refined.auto._

/**
 * Action to create a new game (a new board).
 * The gema is populated with the number of mines indicated.
 */
class CreateBoard(boardRepository: BoardRepository) {
  def apply(rows: Board.Size, cols: Board.Size, mines: Board.Quantity = 0): Result[Board] = {
    val board = Board(rows, cols)
    val b = if (mines.value == 0) board else board.addMines(mines)
    boardRepository.save(b)
  }
}

object CreateBoard {
  def apply(boardRepository: BoardRepository): CreateBoard = new CreateBoard(boardRepository)
}
