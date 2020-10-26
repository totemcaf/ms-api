package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, Result}

class BoardRepositoryStub extends BoardRepository {
  var board: Option[Board] = None

  override def save(board: Board): Board = {
    this.board = Some(board)
    board
  }

  override def findBoard(): Result[Board] = Right(board)
}
