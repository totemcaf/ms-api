package carlosfau.minesweeper

import carlosfau.minesweeper.business.action.{CreateBoard, FlagSquare, UncoverSquare}
import carlosfau.minesweeper.infrastructure.delivery.Server
import carlosfau.minesweeper.infrastructure.repository.InMemoryBoardRepository

object Main extends App {
  val boardRepository = new InMemoryBoardRepository
  val createBoard = CreateBoard(boardRepository)
  val flagSquare = FlagSquare(boardRepository)
  val uncoverSquare = UncoverSquare(boardRepository)

  new Server(
    boardRepository.findBoard,
    createBoard.apply,
    flagSquare.apply,
    uncoverSquare.apply
  ).main(Array.empty) // TODO Pass parameters
}