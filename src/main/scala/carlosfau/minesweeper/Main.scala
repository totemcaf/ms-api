package carlosfau.minesweeper

import carlosfau.minesweeper.business.action.{CreateBoard, FlagSquare, UncoverSquare}
import carlosfau.minesweeper.infrastructure.delivery.Server
import carlosfau.minesweeper.infrastructure.repository.InMemoryAccountRepository
import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val accountRepository = new InMemoryAccountRepository
    
    new Server(
      account => accountRepository.findAccount(account).findBoard,
      account => accountRepository.findAccount(account).listBoards,
      account => CreateBoard(accountRepository.findAccount(account)).apply,
      account => FlagSquare(accountRepository.findAccount(account)) .apply,
      account => UncoverSquare(accountRepository.findAccount(account)).apply
    ).run()
  }
}
