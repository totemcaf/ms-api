package carlosfau.minesweeper.infrastructure

package object delivery {
  import carlosfau.minesweeper.business.model.Board

  case class CreateBody(rows: Int,
                        cols: Int,
                       mines: Int)

  case class BoardView(
                        id: String,
                        rows: Int,
                        cols: Int,
                        state: String,
                        cells: List[List[String]],
                        minesToFind: Int
                      )

  case class FlagCell(
                       row: Int,
                       col: Int,
                       flag: String
                     )

  case class UncoverCell(
                       row: Int,
                       col: Int
                     )

  implicit class ModelToView(board: Board) {

    val toView: BoardView =
      BoardView(
        board.id,
        board.rows.value,
        board.cols.value,
        board.state.toString,
        board.squares.map(_.toString).grouped(board.cols.value).map(_.toList).toList,
        board.minesToFind
      )
  }

}
