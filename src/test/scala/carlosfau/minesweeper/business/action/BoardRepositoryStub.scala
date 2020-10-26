package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, Result}

class BoardRepositoryStub extends BoardRepository {
  var optBoard: Option[Board] = None

  override def save(board: Board): Board = {
    this.optBoard = Some(board)
    board
  }

  override def findBoard(): Result[Board] = Right(optBoard)
}
