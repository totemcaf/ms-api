package carlosfau.minesweeper.infrastructure

package object delivery {
  import carlosfau.minesweeper.business.model.Board

  case class CreateBody(rows: Int,
                        cols: Int,
                       mines: Int)

  case class BoardView(
                        rows: Int,
                        cols: Int,
                        cells: List[List[String]]
                      )

  implicit class ModelToView(board: Board) {

    val toView: BoardView =
      BoardView(
        board.rows.value,
        board.cols.value,
        board.squares.map(_.toString).grouped(board.cols.value).map(_.toList).toList
      )
  }

}
