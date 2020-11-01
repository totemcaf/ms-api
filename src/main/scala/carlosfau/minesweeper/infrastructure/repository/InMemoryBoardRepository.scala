package carlosfau.minesweeper.infrastructure.repository

import java.util.concurrent.ConcurrentHashMap

import carlosfau.minesweeper.business.action.BoardRepository
import carlosfau.minesweeper.business.model.{Board, GameError, Result}
import scala.collection.JavaConverters._

/**
 * Simple In Memory implementation of a Board Repository
 */
class InMemoryBoardRepository extends BoardRepository {
  var boards = new ConcurrentHashMap[Board.ID, Board]

  override def save(board: Board): Result[Board] = boards.compute(board.id, { (_, _) => board }).asResult

  override def findBoard(id: Board.ID): Result[Board] = Right(Option(boards.get(id)))

  override def listBoards(): Either[GameError, List[Board]] = Right(boards.values().asScala.toList)
}
