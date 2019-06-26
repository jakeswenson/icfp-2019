package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.core.Analyzer
import icfp2019.model.BoardCell
import icfp2019.model.GameState
import icfp2019.model.RobotId
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge

object ShortestPathUsingDijkstra : Analyzer<ShortestPathAlgorithm<BoardCell, DefaultEdge>> {
    val cache = Cache.forBoard<(robotId: RobotId, state: GameState) -> ShortestPathAlgorithm<BoardCell, DefaultEdge>> {
        { robotId, state ->
            val completeGraph = BoardCellsGraphAnalyzer.analyze(state)
            val graph = completeGraph(robotId, state) withWeights byState(state.boardState()) { it.point }
            DijkstraShortestPath(graph)
        }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> ShortestPathAlgorithm<BoardCell, DefaultEdge> {
        return cache(initialState.board())
    }
}
