package icfp2019.analyzers

import icfp2019.model.Point
import org.jgrapht.Graph
import org.jgrapht.graph.AsSubgraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.pcollections.PVector

fun <V> Sequence<V>.toGraph(
    neighbors: (V) -> Sequence<V>
): Graph<V, DefaultEdge> {
    val graph = SimpleGraph<V, DefaultEdge>(null, { DefaultEdge() }, true)
    val allCells = this.toList()

    allCells.forEach {
        graph.addVertex(it)
    }

    allCells.forEach { n1 ->
        val allNeighbors = neighbors(n1)
        allNeighbors.forEach {
            val addEdge = graph.addEdge(n1, it)
            if (addEdge != null)
                graph.setEdgeWeight(addEdge, 1.0)
        }
    }

    return graph
}

fun <V, E> Graph<V, E>.filter(predicate: (V) -> Boolean): AsSubgraph<V, E> {
    return AsSubgraph(this, this.vertexSet().filter(predicate).toSet())
}

fun <V, E> Graph<V, E>.copy(weighted: Boolean = false): SimpleGraph<V, E> {
    val simpleGraph = SimpleGraph<V, E>(vertexSupplier, edgeSupplier, weighted)
    vertexSet().forEach { simpleGraph.addVertex(it) }
    edgeSet().forEach { edge ->
        val nodes = this(edge)
        simpleGraph.addEdge(nodes.source, nodes.target).let { newEdge ->
            if (weighted)
                simpleGraph.setEdgeWeight(newEdge, if (this.type.isWeighted) getEdgeWeight(edge) else 1.0)
        }
    }

    return simpleGraph
}

operator fun <V, E> Graph<V, E>.invoke(edge: E): EdgeVertices<V> {
    return EdgeVertices(getEdgeSource(edge), getEdgeTarget(edge))
}

fun <V> Point.lookupIn(board: PVector<PVector<V>>): V {
    return board[x][y]
}

fun <V, E, R> Graph<V, E>.mapVertices(function: (vertex: V) -> R): Graph<R, E> {
    val simpleGraph = SimpleGraph<R, E>(null, edgeSupplier, this.type.isWeighted)
    vertexSet().forEach { simpleGraph.addVertex(function(it)) }
    edgeSet().forEach { edge ->
        val nodes = this(edge)
        simpleGraph.addEdge(function(nodes.source), function(nodes.target))
    }

    return simpleGraph
}
