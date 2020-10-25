package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board
import eu.timepit.refined.auto._

/**
 * Action to create a new game (a new board).
 * The gema is populated with the number of mines indicated.
 */
class CreateBoard {
  def apply(rows: Board.Size, cols: Board.Size, mines: Board.Quantity = 0): Board = {
    val board = Board(rows, cols)
    if (mines == 0) board else board.addMines(mines)
  }
}

object CreateBoard {
  def apply(): CreateBoard = new CreateBoard()
}
