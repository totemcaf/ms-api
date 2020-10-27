package carlosfau.minesweeper

import carlosfau.minesweeper.business.action.{CreateBoard, FlagSquare, UncoverSquare}
import carlosfau.minesweeper.infrastructure.delivery.Server
import carlosfau.minesweeper.infrastructure.repository.InMemoryBoardRepository
import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val boardRepository = new InMemoryBoardRepository
    val createBoard = CreateBoard(boardRepository)
    val flagSquare = FlagSquare(boardRepository)
    val uncoverSquare = UncoverSquare(boardRepository)

    new Server(
      boardRepository.findBoard,
      createBoard.apply,
      flagSquare.apply,
      uncoverSquare.apply
    ).run()
  }
}
