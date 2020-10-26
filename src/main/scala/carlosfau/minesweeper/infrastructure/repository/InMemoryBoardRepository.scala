package carlosfau.minesweeper.infrastructure.repository

import carlosfau.minesweeper.business.action.BoardRepository
import carlosfau.minesweeper.business.model.{Board, Result}

class InMemoryBoardRepository extends BoardRepository {
  var optBoard: Option[Board] = None

  override def save(board: Board): Board = {
    this.optBoard = Some(board)
    board
  }

  override def findBoard(): Result[Board] = Right(optBoard)
}
