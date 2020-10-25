package carlosfau.mw.business.action

import carlosfau.mw.business.model.Board

class CreateBoard {
  def apply(rows: Board.Size, cols: Board.Size): Board = Board(rows, cols)

}

object CreateBoard {
  def apply(): CreateBoard = new CreateBoard()
}
