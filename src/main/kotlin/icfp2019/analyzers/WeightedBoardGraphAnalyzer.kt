package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.core.Analyzer
import icfp2019.model.BoardCell
import icfp2019.model.GameState
import icfp2019.model.RobotId
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph

fun <V, E> Graph<V, E>.copy(weighted: Boolean = false): SimpleGraph<V, E> {
    val simpleGraph = SimpleGraph<V, E>(vertexSupplier, edgeSupplier, weighted)
    vertexSet().forEach { simpleGraph.addVertex(it) }
    edgeSet().forEach { edge ->
        simpleGraph.addEdge(getEdgeSource(edge), getEdgeTarget(edge))
    }

    return simpleGraph
}

fun <V, E> Graph<V, E>.getEdgeVertices(edge: E): Pair<V, V> {
    return (getEdgeSource(edge) to getEdgeTarget(edge))
}

object WeightedBoardGraphAnalyzer : Analyzer<Graph<BoardCell, DefaultEdge>> {
    private val cache = Cache.forGameState { state ->
        val copy = BoardCellsGraphAnalyzer.cache(state.board()).copy(weighted = true)

        copy.edgeSet().forEach {
            val edgeVertices = copy.getEdgeVertices(it)

            copy.setEdgeWeight(it,
                if (listOf(
                        edgeVertices.first,
                        edgeVertices.second
                    ).map { state.nodeState(it.point) }.any { it.isWrapped }
                ) 1.5 else 1.0)
        }

        copy
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Graph<BoardCell, DefaultEdge> {
        return { _, state -> cache(state) }
    }
}
