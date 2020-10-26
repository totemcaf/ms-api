package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, Result}
import carlosfau.minesweeper.business.model.Board.SquareCoordinate

class FlagSquare(boardRepository: BoardRepository) {
  def apply(row: SquareCoordinate, col: SquareCoordinate): Result[Board] =
    boardRepository
      .findBoard()
      .flatMap2(_.flagAt(row, col))
      .map2{ boardRepository.save }
}

object FlagSquare {
  def apply(boardRepository: BoardRepository): FlagSquare = new FlagSquare(boardRepository)
}
