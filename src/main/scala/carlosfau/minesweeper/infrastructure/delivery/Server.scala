package carlosfau.minesweeper.infrastructure.delivery

import carlosfau.minesweeper.business.model.Board.{Flagged, Quantity, Size, SquareCoordinate}
import carlosfau.minesweeper.business.model.{Board, Result => MResult}
import cats.effect.IO
import fs2.StreamApp
import io.circe.generic.auto._
import org.http4s.{EntityDecoder, EntityEncoder, HttpService}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global



class Server(
            boardFinder: () => MResult[Board],
            createBoard: (Size, Size, Quantity) => Board,
            flagSquare: (SquareCoordinate, SquareCoordinate, Flagged) => MResult[Board],
            uncoverSquare: (SquareCoordinate, SquareCoordinate) => MResult[Board]
            )
  extends StreamApp[IO] with Http4sDsl[IO] {

  private implicit val createDecoder: EntityDecoder[IO, CreateBody] = jsonOf[IO, CreateBody]
  private implicit val boardViewEncoder: EntityEncoder[IO, BoardView] = jsonEncoderOf[IO, BoardView]

  val Game = "game"
  val CREATE = "Create"
  val READ = "Read"

  private val root = Root / Game

  private val service = HttpService[IO] {
    case request@GET -> root / id =>
      boardFinder() match {
        case Left(error) => InternalServerError(error.msg)
        case Right(None) => NotFound()
        case Right(Some(board)) => Ok(board.toView)
      }

    case request@POST -> root =>
      val temp: IO[BoardView] = request.as[CreateBody].map(c =>
        createBoard(Size.unsafeFrom(c.rows), Size.unsafeFrom(c.cols), Quantity.unsafeFrom(c.mines)).toView    // TODO manage errors
      )
      temp.flatMap(Created(_))
  }

  def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(service, "/")
      .serve
}
