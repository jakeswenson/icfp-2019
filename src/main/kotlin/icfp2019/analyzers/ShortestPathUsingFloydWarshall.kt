package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.core.Analyzer
import icfp2019.model.GameState
import icfp2019.model.Point
import icfp2019.model.RobotId
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths
import org.jgrapht.graph.DefaultEdge

object ShortestPathUsingFloydWarshall : Analyzer<ShortestPathAlgorithm<Point, DefaultEdge>> {
    val cache = Cache.forGameState { board ->
        val graph =
            BoardCellsGraphAnalyzer.cache.invoke(board.board())
                .mapVertices { it.point } withWeights byState(board.boardState()) { it }

        val shortestPaths = FloydWarshallShortestPaths(graph)
        shortestPaths
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> ShortestPathAlgorithm<Point, DefaultEdge> {
        return { _, _ -> cache(initialState) }
    }
}
