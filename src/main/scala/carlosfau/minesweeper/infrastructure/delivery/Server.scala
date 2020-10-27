package carlosfau.minesweeper.infrastructure.delivery

import carlosfau.minesweeper.business.model.Board.{Flagged, Quantity, Size, SquareCoordinate}
import carlosfau.minesweeper.business.model.{Board, Result => MResult}
import cats.effect.IO
import fs2.StreamApp
import io.circe.generic.auto._
import org.http4s.{EntityDecoder, EntityEncoder, HttpService, Request, Response}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class Server(
            boardFinder: () => MResult[Board],
            createBoard: (Size, Size, Quantity) =>  MResult[Board],
            flagSquare: (SquareCoordinate, SquareCoordinate, Flagged) => MResult[Board],
            uncoverSquare: (SquareCoordinate, SquareCoordinate) => MResult[Board]
            )
  extends StreamApp[IO] with Http4sDsl[IO] {

  private implicit val createDecoder: EntityDecoder[IO, CreateBody] = jsonOf[IO, CreateBody]
  private implicit val flagDecoder: EntityDecoder[IO, FlagCell] = jsonOf[IO, FlagCell]
  private implicit val uncoverDecoder: EntityDecoder[IO, UncoverCell] = jsonOf[IO, UncoverCell]
  private implicit val boardViewEncoder: EntityEncoder[IO, BoardView] = jsonEncoderOf[IO, BoardView]

  val Game = "game"
  val Flags = "flags"
  val Uncovers = "uncovers"

  private val root = Root / Game


  private val service = HttpService[IO] {
    case request@POST -> root / id / Uncovers => handleUncoverSquare(request, id)
    case request@POST -> root / id / Flags => handleFlagSquare(request, id)
    case request@GET -> root / id => handleGetGame
    case request@POST -> root => handleCreateGame(request)

  }

  private def handleCreateGame(request: Request[IO]) = {
    val temp = request.as[CreateBody].map(c => for {
        rows <- Size.from(c.rows)
        cols <- Size.from(c.cols)
        mines <- Quantity.from(c.mines)
      } yield createBoard(rows, cols, mines)
    )
    temp.flatMap(_.fold(BadRequest(_), mapActionResult))
  }

  private def handleGetGame = mapActionResult(boardFinder())

  def handleFlagSquare(request: Request[IO], id: String): IO[Response[IO]] = {
    val temp = request.as[FlagCell].map(c => for {
          row <- SquareCoordinate.from(c.row)
          col <- SquareCoordinate.from(c.col)
          flag <- Flagged.from(c.flag)
      } yield flagSquare(row, col, flag)
    )

    temp.flatMap(_.fold(BadRequest(_), mapActionResult)
    )
  }

  def handleUncoverSquare(request: Request[IO], id: String): IO[Response[IO]] = {
    val temp = request.as[FlagCell].map(c => for {
        row <- SquareCoordinate.from(c.row)
        col <- SquareCoordinate.from(c.col)
      } yield uncoverSquare(row, col)
    )

    temp.flatMap(_.fold(BadRequest(_), mapActionResult)
    )
  }

  private def mapActionResult(result: MResult[Board]) = result match {
    case Left(error) => InternalServerError(error.msg)
    case Right(None) => NotFound()
    case Right(Some(board)) => Ok(board.toView)
  }

  def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(service, "/")
      .serve
}
