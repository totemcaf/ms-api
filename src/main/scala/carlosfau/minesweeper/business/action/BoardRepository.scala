package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, Result}

trait BoardRepository {
  def save(board: Board): Board
  def findBoard(): Result[Board]
}
