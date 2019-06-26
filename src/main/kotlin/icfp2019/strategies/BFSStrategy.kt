package icfp2019.strategies

import icfp2019.analyzers.BoardCellsGraphAnalyzer
import icfp2019.analyzers.Path
import icfp2019.analyzers.PointShortestPathsAnalyzer
import icfp2019.core.Strategy
import icfp2019.model.*
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.BreadthFirstIterator
import org.jgrapht.traverse.GraphIterator
import java.util.*

private fun <V, E> GraphPath<V, E>.second(): V {
    return this.vertexList[1]
}

fun <T> Sequence<T>.sample(count: Int): Sequence<T> = toMutableList()
    .shuffled()
    .take(count)
    .asSequence()

object BFSStrategy : Strategy {
    override fun compute(initialState: GameState): (robotId: RobotId, state: GameState) -> Action {
        val graphBuilder = BoardCellsGraphAnalyzer.analyze(initialState)
        val pointShortestPaths = PointShortestPathsAnalyzer.analyze(initialState)
        val cloningLocations = initialState.boardState().allStates()
            .filter { it.hasBooster(Booster.CloningLocation) }.toList()

        return { robotId, gameState ->
            val shortestPaths = pointShortestPaths(robotId, gameState)
            val currentPoint = gameState.robot(robotId).currentPosition
            val currentNode = gameState.get(currentPoint)
            val pathsFromCurrentNode = lazy {
                pointShortestPaths(robotId, gameState)
            }

            val tokens = gameState.boardState().allStates()
                .filter { it.hasBooster(Booster.CloneToken) }.toList()

            if (gameState.backpackContains(Booster.CloneToken) && cloningLocations.any()) {
                moveToNearestClonePoint(gameState, robotId, cloningLocations, pathsFromCurrentNode, currentPoint)
            } else if (tokens.any()) {
                moveToCloningToken(tokens, pathsFromCurrentNode, currentPoint)
            } else {
                fun isValid(point: Point): Boolean {
                    return gameState.isInBoard(point) &&
                            gameState.nodeState(point).isWrapped.not() &&
                            gameState.get(point).isObstacle.not()
                }

                val neighbors = currentNode.point.neighbors().filter(::isValid).toList().shuffled(Random(robotId.id.toLong()))

                if (neighbors.any()) {
                    currentPoint.actionToGetToNeighbor(neighbors.first())
                } else {
                    val graph = graphBuilder(robotId, gameState)
                    moveToClosestNode(graph, currentNode, gameState, currentPoint) {
                        shortestPaths.pointSource.getPath(it).vertexList
                    }
                }
            }
        }
    }

    private fun moveToClosestNode(
        graph: Graph<BoardCell, DefaultEdge>,
        currentNode: BoardCell,
        gameState: GameState,
        currentPoint: Point,
        shortestPaths: (Point) -> List<Point>
    ): Action {
        val bfsIterator: GraphIterator<BoardCell, DefaultEdge> = BreadthFirstIterator(graph, currentNode)

        var seenUnwrapped = false
        var node: BoardCell? = null
        for (boardCell in bfsIterator) {
            if (seenUnwrapped) break
            seenUnwrapped = !gameState.nodeState(boardCell.point).isWrapped
            node = boardCell
        }

        val path = shortestPaths(node!!.point)
        return currentPoint.actionToGetToNeighbor(path[1])
    }

    private fun moveToCloningToken(
        tokens: List<BoardNodeState>,
        pathsFromCurrentNode: Lazy<Path>,
        currentPoint: Point
    ): Action {
        val shortestPath = tokens.map {
            pathsFromCurrentNode.value.pointSource.getPath(it.point)
        }.minBy { it.length }!!
        return currentPoint.actionToGetToNeighbor(shortestPath.second())
    }

    private fun moveToNearestClonePoint(
        gameState: GameState,
        robotId: RobotId,
        cloningLocations: List<BoardNodeState>,
        pathsFromCurrentNode: Lazy<Path>,
        currentPoint: Point
    ): Action {
        return if (gameState.robotIsOn(robotId, Booster.CloningLocation)) Action.CloneRobot
        else {
            val shortestPath = cloningLocations.map {
                pathsFromCurrentNode.value.pointSource.getPath(it.point)
            }.minBy { it.length }!!
            currentPoint.actionToGetToNeighbor(shortestPath.second())
        }
    }
}
