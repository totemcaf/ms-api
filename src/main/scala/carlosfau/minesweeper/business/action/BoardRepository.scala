package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, Result}

trait BoardRepository {
  def save(board: Board): Result[Board]
  def findBoard(): Result[Board]
}
