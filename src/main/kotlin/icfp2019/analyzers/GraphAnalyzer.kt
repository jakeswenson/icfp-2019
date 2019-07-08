package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.GraphEdge
import icfp2019.core.Analyzer
import icfp2019.core.contains
import icfp2019.core.get
import icfp2019.model.*
import icfp2019.toGraph
import org.jgrapht.Graph

object GraphAnalyzer : Analyzer<Graph<BoardCell, GraphEdge>> {
    fun graphFor(board: Board): (MovementSpeed) -> Graph<BoardCell, GraphEdge> {
        return { movementSpeed: MovementSpeed ->
            when (movementSpeed) {
                MovementSpeed.Fast -> FastMoveBoardCellsGraphAnalyzer.cache(board)
                MovementSpeed.Normal -> BoardCellsGraphAnalyzer.cache(board)
            }
        }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Graph<BoardCell, GraphEdge> {
        return { robotId, gameState ->
            graphFor(gameState.board())(gameState.robot(robotId).speed())
        }
    }
}

private object BoardCellsGraphAnalyzer : Analyzer<Graph<BoardCell, GraphEdge>> {
    internal val cache = Cache.forBoard { board ->
        board.allCells().filter { it.isObstacle.not() }
            .toGraph { cell ->
                cell.point.neighbors()
                    .filter { it.move in board && board[it.move].isObstacle.not() }
                    .map { it.move.lookupIn(board) }
            }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Graph<BoardCell, GraphEdge> {
        return { _, state -> cache(state.board()) }
    }
}

private object FastMoveBoardCellsGraphAnalyzer : Analyzer<Graph<BoardCell, GraphEdge>> {
    internal val cache = Cache.forBoard { board ->
        board.allCells().filter { it.isObstacle.not() }
            .toGraph { cell ->
                fun Point.isValid(): Boolean = this in board && board[this].isObstacle.not()
                cell.point.neighbors()
                    .flatMap {
                        sequence {
                            if (it.fastMove.isValid()) yield(it.fastMove.lookupIn(board))
                            else if (it.move.isValid()) yield(it.move.lookupIn(board))
                        }
                    }
            }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Graph<BoardCell, GraphEdge> {
        return { _, state -> cache(state.board()) }
    }
}
