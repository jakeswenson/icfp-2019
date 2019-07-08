package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.GraphEdge
import icfp2019.core.Analyzer
import icfp2019.mapVertices
import icfp2019.model.GameState
import icfp2019.model.Point
import icfp2019.model.RobotId
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths
import java.time.Duration

inline fun <T> measureTime(block: () -> T): Pair<T, Duration> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to Duration.ofMillis(System.currentTimeMillis() - start)
}

object ShortestPathUsingFloydWarshall : Analyzer<ShortestPathAlgorithm<Point, GraphEdge>> {
    val cache = Cache.forBoardAndSpeed { pair ->
        val weightedGraphBuilder = WeightedBoardGraphAnalyzer.weightedGraph(pair.first, pair.second)
        val weightCache = Cache.forBoardState { states ->
            val weightedGraph = weightedGraphBuilder(states).mapVertices { it.point }
            val (paths, time) = measureTime {
                val shortestPaths = FloydWarshallShortestPaths(weightedGraph)
                println("Found ${shortestPaths.shortestPathsCount} shortest paths")
                shortestPaths
            }
            println("Took ${time.seconds} seconds to find shorest paths")
            paths
        }

        weightCache
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> ShortestPathAlgorithm<Point, GraphEdge> {
        return { robotId, state -> cache(initialState.board() to state.robot(robotId).speed())(initialState.boardState()) }
    }
}
