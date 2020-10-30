package carlosfau.minesweeper.infrastructure.delivery

import carlosfau.minesweeper.business.model.Board.{Flagged, Quantity, Size, SquareCoordinate}
import carlosfau.minesweeper.business.model.{Board, Result => MResult}
import cats.effect.{IO, _}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.middleware._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

class Server(
            boardFinder: Board.ID => MResult[Board],
            createBoard: (Size, Size, Quantity) =>  MResult[Board],
            flagSquare: (Board.ID, SquareCoordinate, SquareCoordinate, Flagged) => MResult[Board],
            uncoverSquare: (Board.ID, SquareCoordinate, SquareCoordinate) => MResult[Board]
            )
  extends Http4sDsl[IO] {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  private implicit val createDecoder: EntityDecoder[IO, CreateBody] = jsonOf[IO, CreateBody]
  private implicit val flagDecoder: EntityDecoder[IO, FlagCell] = jsonOf[IO, FlagCell]
  private implicit val uncoverDecoder: EntityDecoder[IO, UncoverCell] = jsonOf[IO, UncoverCell]
  private implicit val boardViewEncoder: EntityEncoder[IO, BoardView] = jsonEncoderOf[IO, BoardView]

  val Version = "v1"
  val Game = "games"
  val Flags = "flags"
  val Uncovers = "uncovers"

  private val root = Root / Version / Game

  private val service =  HttpRoutes.of[IO] {
    case GET -> Root / "health" => handleHealth
    case GET -> `root` / id => handleGetGame(id)
    case request@POST -> `root` / id / Uncovers => handleUncoverSquare(request, id)
    case request@POST -> `root` / id / Flags => handleFlagSquare(request, id)
    case request@POST -> `root` => handleCreateGame(request)
  }.orNotFound

  private def handleHealth: IO[Response[IO]] = Ok("Minesweeper is Ok")

  private def handleCreateGame(request: Request[IO]) = {
    val temp = request.as[CreateBody].map(c => for {
        rows <- Size.from(c.rows)
        cols <- Size.from(c.cols)
        mines <- Quantity.from(c.mines)
      } yield createBoard(rows, cols, mines)
    )
    temp.flatMap(_.fold(BadRequest(_), mapActionResult))
  }

  private def handleGetGame(id: String) = mapActionResult(boardFinder(id))

  def handleFlagSquare(request: Request[IO], id: String): IO[Response[IO]] = {
    val temp = request.as[FlagCell].map(c => for {
          row <- SquareCoordinate.from(c.row)
          col <- SquareCoordinate.from(c.col)
          flag <- Flagged.from(c.flag)
      } yield flagSquare(id, row, col, flag)
    )

    temp.flatMap(_.fold(BadRequest(_), mapActionResult)
    )
  }

  def handleUncoverSquare(request: Request[IO], id: String): IO[Response[IO]] = {
    val temp = request.as[UncoverCell].map(c => for {
        row <- SquareCoordinate.from(c.row)
        col <- SquareCoordinate.from(c.col)
      } yield uncoverSquare(id, row, col)
    )

    temp.flatMap(_.fold(BadRequest(_), mapActionResult)
    )
  }

  private def mapActionResult(result: MResult[Board]) = result match {
    case Left(error) => InternalServerError(error.msg)
    case Right(None) => NotFound()
    case Right(Some(board)) => Ok(board.toView)
  }

  private val corsConfig = CORSConfig(
    anyOrigin = true,
    allowedOrigins = {
      case "http://localhost:3000" => true
      case "http://ec2-3-87-195-146.compute-1.amazonaws.com" => true
      case _ => false
    },
    anyMethod = false,
    allowedMethods = Some(Set("GET", "POST")),
    allowCredentials = true,
    maxAge = 1.day.toSeconds)

  def run(): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(CORS(service, corsConfig))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
