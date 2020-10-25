package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board
import carlosfau.minesweeper.business.model.Board.SquareCoordinate

class MarkSquare(boardRepository: BoardRepository) {

  def apply(row: SquareCoordinate, col: SquareCoordinate): Option[Board] =
    boardRepository
      .findBoard()
      .map(_.markAt(row, col))
      .map{ boardRepository.save }
}

object MarkSquare {
  def apply(boardRepository: BoardRepository): MarkSquare = new MarkSquare(boardRepository)
}
