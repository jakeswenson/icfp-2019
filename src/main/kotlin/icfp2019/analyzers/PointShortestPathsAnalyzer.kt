package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.core.Analyzer
import icfp2019.model.BoardCell
import icfp2019.model.GameState
import icfp2019.model.Point
import icfp2019.model.RobotId
import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge

data class Path(val pointSource: ShortestPathAlgorithm.SingleSourcePaths<Point, DefaultEdge>)

object PointShortestPathsAnalyzer : Analyzer<Path> {
    val shortestPathsCache = Cache.forGameState { st ->
        val graph: Graph<BoardCell, DefaultEdge> = BoardCellsGraphAnalyzer.cache(st.board())
        val graphPoints = graph.withWeights(byState(st.boardState()) { it.point })
            .mapVertices { it.point }
        val shortestPaths = DijkstraShortestPath(graphPoints)
        shortestPaths
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Path {
        return { robotId, graphState ->
            val currentPosition = graphState.robot(robotId).currentPosition
            val currentShortestPaths = shortestPathsCache(initialState).getPaths(currentPosition)
            Path(currentShortestPaths)
        }
    }
}
