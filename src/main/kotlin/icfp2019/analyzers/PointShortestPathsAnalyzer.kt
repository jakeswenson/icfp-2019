package icfp2019.analyzers

import icfp2019.GraphEdge
import icfp2019.core.Analyzer
import icfp2019.model.GameState
import icfp2019.model.Point
import icfp2019.model.RobotId
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm

data class Path(val pointSource: ShortestPathAlgorithm.SingleSourcePaths<Point, GraphEdge>)

object PointShortestPathsAnalyzer : Analyzer<Path> {
    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Path {
        val shorestPaths = ShortestPathUsingFloydWarshall.analyze(initialState)
        return { robotId, state ->
            val paths = shorestPaths(robotId, state)
            val currentPosition = state.robot(robotId).currentPosition
            val currentShortestPaths = paths.getPaths(currentPosition)
            Path(currentShortestPaths)
        }
    }
}
