package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.core.Analyzer
import icfp2019.model.*
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

typealias WeightFunction<V> = (node1: V) -> (node2: V) -> Double
fun <V> byState(boardStates: BoardNodeStates, locationFor: (V) -> Point): WeightFunction<V> = { n1 ->
    val n1State = locationFor(n1).lookupIn(boardStates)
    when {
        n1State.isWrapped -> { _ -> 1.5 }
        n1State.hasBooster -> { _ -> 0.5 }
        else -> { n2 ->
            val n2State = locationFor(n2).lookupIn(boardStates)
            when {
                n2State.isWrapped -> 1.5
                n2State.hasBooster -> 0.5
                else -> 1.0
            }
        }
    }
}

infix fun <V, E> Graph<V, E>.withWeights(weight: WeightFunction<V>): Graph<V, E> {
    val simpleGraph = this.copy(weighted = true)

    simpleGraph.edgeSet().forEach { edge ->
        val nodes = simpleGraph(edge)
        simpleGraph.setEdgeWeight(
            edge, weight(nodes.source)(nodes.target)
        )
    }

    return simpleGraph
}

data class EdgeVertices<V>(val source: V, val target: V)

fun <V, E> Graph<V, E>.getEdgeVertices(edge: E): EdgeVertices<V> {
    return EdgeVertices(getEdgeSource(edge), getEdgeTarget(edge))
}

object WeightedBoardGraphAnalyzer : Analyzer<Graph<BoardCell, DefaultEdge>> {
    private val cache = Cache.forGameState { state ->
        val copy = BoardCellsGraphAnalyzer.cache(state.board()).copy(weighted = true)

        copy withWeights byState(state.boardState()) { it.point }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Graph<BoardCell, DefaultEdge> {
        return { _, state -> cache(state) }
    }
}
