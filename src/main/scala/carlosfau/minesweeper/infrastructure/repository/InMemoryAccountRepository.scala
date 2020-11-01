package carlosfau.minesweeper.infrastructure.repository

import java.util.concurrent.ConcurrentHashMap

import carlosfau.minesweeper.business.action.BoardRepository
import carlosfau.minesweeper.business.model.{AccountId, Board, Result}

/**
 * Simple In Memory implementation of an Account Repository.
 * This implementation grows over the BoardRepository having one repo per account.
 */
class InMemoryAccountRepository {
  var boards = new ConcurrentHashMap[AccountId, BoardRepository]

  def findAccount(accountId: AccountId): BoardRepository =
    boards.computeIfAbsent(accountId, _ => new InMemoryBoardRepository)
}
