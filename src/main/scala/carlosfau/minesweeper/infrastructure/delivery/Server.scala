package carlosfau.minesweeper.infrastructure.delivery

import carlosfau.minesweeper.business.model.Board.{Flagged, Quantity, Size, SquareCoordinate}
import carlosfau.minesweeper.business.model.{AccountId, Board, GameError, Result => MResult}
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
            boardFinder: AccountId => Board.ID => MResult[Board],
            boardLister: AccountId => () => Either[GameError, List[Board]],
            createBoard: AccountId => (Size, Size, Quantity) =>  MResult[Board],
            flagSquare: AccountId => (Board.ID, SquareCoordinate, SquareCoordinate, Flagged) => MResult[Board],
            uncoverSquare: AccountId => (Board.ID, SquareCoordinate, SquareCoordinate) => MResult[Board]
            )
  extends Http4sDsl[IO] {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  private implicit val createDecoder: EntityDecoder[IO, CreateBody] = jsonOf[IO, CreateBody]
  private implicit val flagDecoder: EntityDecoder[IO, FlagCell] = jsonOf[IO, FlagCell]
  private implicit val uncoverDecoder: EntityDecoder[IO, UncoverCell] = jsonOf[IO, UncoverCell]
  private implicit val boardViewEncoder: EntityEncoder[IO, BoardView] = jsonEncoderOf[IO, BoardView]
  private implicit val boardViewsEncoder: EntityEncoder[IO, List[BoardView]] = jsonEncoderOf[IO, List[BoardView]]

  val Version = "v2"
  val Account = "accounts"
  val Games = "games"
  val Flags = "flags"
  val Uncovers = "uncovers"

  private val root = Root / Version / Account

  private val service =  HttpRoutes.of[IO] {
    case GET -> Root / "health" => handleHealth

    case GET -> `root` / accountId / `Games` / id => handleGetGame(accountId, id)
    case GET -> `root` / accountId / `Games` => handleListGames(accountId)
    case request@POST -> `root` / accountId / `Games` / id / Uncovers => handleUncoverSquare(request, accountId, id)
    case request@POST -> `root` / accountId / `Games` / id / Flags => handleFlagSquare(request, accountId, id)
    case request@POST -> `root` / accountId / `Games` => handleCreateGame(request, accountId)
  }.orNotFound

  private def handleHealth: IO[Response[IO]] = Ok("Minesweeper is Ok")

  private def handleCreateGame(request: Request[IO], accountIdStr: String) = {
    val temp = request.as[CreateBody].map(c => for {
        accountId <- AccountId.from(accountIdStr)
        rows <- Size.from(c.rows)
        cols <- Size.from(c.cols)
        mines <- Quantity.from(c.mines)
      } yield createBoard(accountId)(rows, cols, mines)
    )
    temp.flatMap(_.fold(BadRequest(_), mapActionResult))
  }

  private def handleGetGame(accountIdStr: String, id: String) =
    (for {
      accountId <- AccountId.from(accountIdStr)
    } yield boardFinder(accountId)(id))
    .fold(BadRequest(_), mapActionResult)

  private def handleListGames(accountIdStr: String) =
    (for {
      accountId <- AccountId.from(accountIdStr)
    } yield boardLister(accountId)())
    .fold(BadRequest(_), mapActionResults)

  def handleFlagSquare(request: Request[IO], accountIdStr: String, id: String): IO[Response[IO]] = {
    val temp = request.as[FlagCell].map(c => for {
          accountId <- AccountId.from(accountIdStr)
          row <- SquareCoordinate.from(c.row)
          col <- SquareCoordinate.from(c.col)
          flag <- Flagged.from(c.flag)
      } yield flagSquare(accountId)(id, row, col, flag)
    )

    temp.flatMap(_.fold(BadRequest(_), mapActionResult)
    )
  }

  def handleUncoverSquare(request: Request[IO], accountIdStr: String, id: String): IO[Response[IO]] = {
    val temp = request.as[UncoverCell].map(c => for {
        accountId <- AccountId.from(accountIdStr)
        row <- SquareCoordinate.from(c.row)
        col <- SquareCoordinate.from(c.col)
      } yield uncoverSquare(accountId)(id, row, col)
    )

    temp.flatMap(_.fold(BadRequest(_), mapActionResult)
    )
  }

  private def mapActionResult(result: MResult[Board]) = result match {
    case Left(error) => InternalServerError(error.msg)
    case Right(None) => NotFound()
    case Right(Some(board)) => Ok(board.toView)
  }

  private def mapActionResults(result: Either[GameError, List[Board]]) = result match {
    case Left(error) => InternalServerError(error.msg)
    case Right(boards) => Ok(boards.map(_.toView))
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
