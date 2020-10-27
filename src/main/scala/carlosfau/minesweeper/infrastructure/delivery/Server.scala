package carlosfau.minesweeper.infrastructure.delivery

import java.nio.file.{Files, Paths}

import carlosfau.minesweeper.business.model.Board.{Flagged, Quantity, Size, SquareCoordinate}
import carlosfau.minesweeper.business.model.{Board, Result => MResult}
import cats.effect.{IO, _}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT

import scala.concurrent.ExecutionContext.global

class Server(
            boardFinder: Board.ID => MResult[Board],
            createBoard: (Size, Size, Quantity) =>  MResult[Board],
            flagSquare: (Board.ID, SquareCoordinate, SquareCoordinate, Flagged) => MResult[Board],
            uncoverSquare: (Board.ID, SquareCoordinate, SquareCoordinate) => MResult[Board]
            )
  extends Http4sDsl[IO] {

  import java.util.concurrent._

  import cats.effect.Blocker

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  private val blockingPool = Executors.newFixedThreadPool(4)
  private val blocker = Blocker.liftExecutorService(blockingPool)

  private implicit val createDecoder: EntityDecoder[IO, CreateBody] = jsonOf[IO, CreateBody]
  private implicit val flagDecoder: EntityDecoder[IO, FlagCell] = jsonOf[IO, FlagCell]
  private implicit val uncoverDecoder: EntityDecoder[IO, UncoverCell] = jsonOf[IO, UncoverCell]
  private implicit val boardViewEncoder: EntityEncoder[IO, BoardView] = jsonEncoderOf[IO, BoardView]

  val Version = "v1"
  val Game = "game"
  val Flags = "flags"
  val Uncovers = "uncovers"

  private val root = Root / Version / Game

  private val staticFilesFolder = "/static/"
  private val localStaticFilePath = "./src/main/resources" + staticFilesFolder
  private val isLocal = Files.exists(Paths.get(localStaticFilePath))

  private val service =  HttpRoutes.of[IO] {
    case GET -> Root / "health" => handleHealth
    case request@GET -> Root / file => handleIndex(file, request)
    case request@GET -> Root => handleIndex("index.html", request)
    case GET -> `root` / id => handleGetGame(id)
    case request@POST -> `root` / id / Uncovers => handleUncoverSquare(request, id)
    case request@POST -> `root` / id / Flags => handleFlagSquare(request, id)
    case request@POST -> `root` => handleCreateGame(request)
  }.orNotFound

  private def handleHealth: IO[Response[IO]] = Ok("Minesweeper is Ok")

  /**
   * This is a very simplified implementation of a static server. Just for this exercise.
   * Putting this behind a CDN could make this better solution.
   */
  private def handleIndex =
    if (isLocal)
      (file: String, request: Request[IO]) => StaticFile.fromFile(Paths.get(localStaticFilePath, file).toFile, blocker, Some(request)).getOrElseF(NotFound())
    else
      (file: String, request: Request[IO]) => StaticFile.fromResource(staticFilesFolder + file, blocker, Some(request)).getOrElseF(NotFound())

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

  def run(): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(service)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
