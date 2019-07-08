package icfp2019

import icfp2019.analyzers.EdgeVertices
import org.jgrapht.Graph
import org.jgrapht.graph.AsSubgraph
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.SimpleWeightedGraph

sealed class GraphEdge {
    class Edge : GraphEdge()
    class TransporterEdge : GraphEdge()
}

fun <V> Sequence<V>.toGraph(
    neighbors: (V) -> Sequence<V>
): Graph<V, GraphEdge> {
    val graph = SimpleGraph<V, GraphEdge>(null, null, false)
    val allCells = this.toList()

    allCells.forEach {
        graph.addVertex(it)
    }

    allCells.forEach { n1 ->
        val allNeighbors = neighbors(n1)
        allNeighbors.forEach {
            graph.addEdge(n1, it, GraphEdge.Edge())
        }
    }

    return graph
}

fun <V, E> Graph<V, E>.filter(predicate: (V) -> Boolean): AsSubgraph<V, E> {
    return AsSubgraph(this, this.vertexSet().filter(predicate).toSet())
}

fun <V, E> Graph<V, E>.copy(weighted: Boolean = false): Graph<V, E> {
    val simpleGraph =
        if (weighted) SimpleWeightedGraph<V, E>(vertexSupplier, edgeSupplier)
        else SimpleGraph<V, E>(vertexSupplier, edgeSupplier, false)

    vertexSet().forEach { simpleGraph.addVertex(it) }

    edgeSet().forEach { edge ->
        val nodes = this[edge]
        simpleGraph.addEdge(nodes.source, nodes.target, edge)
        if (weighted) simpleGraph.setEdgeWeight(
            edge,
            if (this.type.isWeighted) getEdgeWeight(edge) else Graph.DEFAULT_EDGE_WEIGHT
        )
    }

    return simpleGraph
}

operator fun <V, E> Graph<V, E>.get(edge: E): EdgeVertices<V> {
    return EdgeVertices(getEdgeSource(edge), getEdgeTarget(edge))
}

fun <V, E, R> Graph<V, E>.mapVertices(function: (vertex: V) -> R): Graph<R, E> {
    val simpleGraph = SimpleGraph<R, E>(null, edgeSupplier, this.type.isWeighted)
    vertexSet().forEach { simpleGraph.addVertex(function(it)) }
    edgeSet().forEach { edge ->
        val nodes = this[edge]
        simpleGraph.addEdge(function(nodes.source), function(nodes.target), edge)
    }

    return simpleGraph
}
