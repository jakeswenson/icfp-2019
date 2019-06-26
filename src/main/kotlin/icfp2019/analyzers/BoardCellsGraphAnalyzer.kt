package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.core.Analyzer
import icfp2019.core.contains
import icfp2019.core.get
import icfp2019.model.BoardCell
import icfp2019.model.GameState
import icfp2019.model.RobotId
import icfp2019.model.allCells
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

object BoardCellsGraphAnalyzer : Analyzer<Graph<BoardCell, DefaultEdge>> {
    internal val cache = Cache.forBoard { board ->
        board.allCells().filter { it.isObstacle.not() }
            .toGraph { cell ->
                cell.point.neighbors().filter {
                    it in board &&
                            board[it].isObstacle.not()
                }.map { it.lookupIn(board) }
            }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Graph<BoardCell, DefaultEdge> {
        return { _, state -> cache(state.board()) }
    }
}
