package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.GraphEdge
import icfp2019.core.Analyzer
import icfp2019.model.BoardCell
import icfp2019.model.GameState
import icfp2019.model.RobotId
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm
import org.jgrapht.alg.shortestpath.DijkstraShortestPath

object ShortestPathUsingDijkstra : Analyzer<ShortestPathAlgorithm<BoardCell, GraphEdge>> {
    val cache = Cache.forBoard<(robotId: RobotId, state: GameState) -> ShortestPathAlgorithm<BoardCell, GraphEdge>> {
        { robotId, state ->
            val completeGraph = GraphAnalyzer.analyze(state)
            val graph = completeGraph(robotId, state) withWeights byState(state.boardState()) { it.point }
            DijkstraShortestPath(graph)
        }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> ShortestPathAlgorithm<BoardCell, GraphEdge> {
        return cache(initialState.board())
    }
}
