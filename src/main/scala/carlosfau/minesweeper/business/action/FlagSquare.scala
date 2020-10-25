package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.Board
import carlosfau.minesweeper.business.model.Board.SquareCoordinate

class FlagSquare(boardRepository: BoardRepository) {

  def apply(row: SquareCoordinate, col: SquareCoordinate): Option[Board] =
    boardRepository
      .findBoard()
      .map(_.markAt(row, col))
      .map{ boardRepository.save }
}

object FlagSquare {
  def apply(boardRepository: BoardRepository): FlagSquare = new FlagSquare(boardRepository)
}
