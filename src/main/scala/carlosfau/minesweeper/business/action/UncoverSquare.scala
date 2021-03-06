package carlosfau.minesweeper.business.action

import carlosfau.minesweeper.business.model.{Board, Result}
import carlosfau.minesweeper.business.model.Board.SquareCoordinate

/**
 * Makes an covered cell, uncovered.
 * If the cell is not uncovered, an error will be reported.
 * If the cell contains a mine, ti will explode and the user will lose the game.
 * In other cases, the cell will show the number of mines in its adjacent squares
 * @param boardRepository the repository to store games
 */
class UncoverSquare(boardRepository: BoardRepository) {
  def apply(id: Board.ID, row: SquareCoordinate, col: SquareCoordinate): Result[Board] =
    boardRepository
      .findBoard(id)
      .flatMap2(_.uncover(row, col))
      .flatMap2(boardRepository.save)
}

object UncoverSquare {
  def apply(boardRepository: BoardRepository): UncoverSquare = new UncoverSquare(boardRepository)
}
