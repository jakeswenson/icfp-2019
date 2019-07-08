package icfp2019.analyzers

import icfp2019.GraphEdge
import icfp2019.copy
import icfp2019.core.Analyzer
import icfp2019.get
import icfp2019.model.*
import org.jgrapht.Graph

typealias WeightFunction<V> = (node1: V) -> (node2: V) -> Double

const val BOOSTER_WEIGHT = -1000.0
const val WRAPPED_WEIGHT = 2.0

fun <V> byState(boardStates: BoardStates, locationFor: (V) -> Point): WeightFunction<V> = { n1 ->
    val n1State = locationFor(n1).lookupIn(boardStates)
    when {
        n1State.hasBooster -> { _ -> BOOSTER_WEIGHT }
        n1State.isWrapped -> { _ -> WRAPPED_WEIGHT }
        else -> { n2 ->
            val n2State = locationFor(n2).lookupIn(boardStates)
            when {
                n2State.hasBooster -> BOOSTER_WEIGHT
                n2State.isWrapped -> WRAPPED_WEIGHT
                else -> Graph.DEFAULT_EDGE_WEIGHT
            }
        }
    }
}

infix fun <V, E> Graph<V, E>.withWeights(weight: WeightFunction<V>): Graph<V, E> {
    val simpleGraph = this.copy(weighted = true)

    simpleGraph.edgeSet().forEach { edge ->
        val nodes = simpleGraph[edge]
        simpleGraph.setEdgeWeight(
            edge, weight(nodes.source)(nodes.target)
        )
    }

    return simpleGraph
}

data class EdgeVertices<V>(val source: V, val target: V)

object WeightedBoardGraphAnalyzer : Analyzer<Graph<BoardCell, GraphEdge>> {
    fun weightedGraph(
        board: Board,
        movementSpeed: MovementSpeed
    ): (boardStates: BoardStates) -> Graph<BoardCell, GraphEdge> {
        val graph = GraphAnalyzer.graphFor(board)(movementSpeed)
        return { boardStates -> graph.copy() withWeights byState(boardStates) { it.point } }
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Graph<BoardCell, GraphEdge> {
        return { robotId, state -> weightedGraph(state.board(), state.robot(robotId).speed())(state.boardState()) }
    }
}
