package icfp2019.analyzers

import icfp2019.GraphEdge
import icfp2019.model.GameState
import icfp2019.core.Analyzer
import icfp2019.model.RobotId
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree

object MSTAnalyzer : Analyzer<SpanningTreeAlgorithm.SpanningTree<GraphEdge>> {
    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> SpanningTreeAlgorithm.SpanningTree<GraphEdge> {
        val completeGraph = WeightedBoardGraphAnalyzer.analyze(initialState)
        return { robotId, state ->
            val graph = completeGraph(robotId, state)
            KruskalMinimumSpanningTree(graph).spanningTree
        }
    }
}
