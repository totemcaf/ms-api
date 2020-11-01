package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, GameError, Result}

trait BoardRepository {
  def save(board: Board): Result[Board]
  def findBoard(id: Board.ID): Result[Board]
  def listBoards(): Either[GameError, List[Board]]
}
