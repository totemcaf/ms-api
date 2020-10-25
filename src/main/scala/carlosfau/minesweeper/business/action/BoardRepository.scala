package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board

trait BoardRepository {
  def save(board: Board): Board
  def findBoard(): Option[Board]
}
