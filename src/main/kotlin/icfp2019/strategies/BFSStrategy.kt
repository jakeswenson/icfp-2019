package icfp2019.strategies

import icfp2019.GraphEdge
import icfp2019.analyzers.Path
import icfp2019.analyzers.PointShortestPathsAnalyzer
import icfp2019.analyzers.WeightedBoardGraphAnalyzer
import icfp2019.core.Strategy
import icfp2019.model.*
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.traverse.BreadthFirstIterator
import org.jgrapht.traverse.GraphIterator
import java.util.*

private fun <V, E> GraphPath<V, E>.second(): V {
    return this.vertexList[1]
}

object BFSStrategy : Strategy {
    override fun compute(initialState: GameState): (robotId: RobotId, state: GameState) -> Action {
        val graphBuilder = WeightedBoardGraphAnalyzer.analyze(initialState)
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
                .filter { it.hasBooster(Booster.CloneToken) }
                .drop(robotId.id) // only one robot chases a token at a time
                .toList()

            if (gameState.backpackContains(Booster.ExtraArm)) {
                Action.AttachManipulator(gameState.robot(robotId).optimumManipulatorArmTarget())
            } else if (gameState.backpackContains(Booster.CloneToken) && cloningLocations.any()) {
                moveToNearestClonePoint(gameState, robotId, cloningLocations, pathsFromCurrentNode, currentPoint)
            } else if (tokens.any()) {
                moveToCloningToken(tokens, pathsFromCurrentNode, currentPoint)
            } else {
                fun isValid(neighbor: NeighborPoint): Boolean {
                    return gameState.isInBoard(neighbor.move) &&
                            gameState.nodeState(neighbor.move).isWrapped.not() &&
                            gameState.get(neighbor.move).isObstacle.not()
                }

                val neighbors = lazy {
                    val list = currentNode.point.neighbors().filter(::isValid).toMutableList()
                    Collections.rotate(list, robotId.id)
                    list.toList()
                }

                if (neighbors.value.any()) {
                    currentPoint.actionToGetToNeighbor(neighbors.value.first().move)
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
        graph: Graph<BoardCell, GraphEdge>,
        currentNode: BoardCell,
        gameState: GameState,
        currentPoint: Point,
        shortestPaths: (Point) -> List<Point>
    ): Action {
        val bfsIterator: GraphIterator<BoardCell, GraphEdge> = BreadthFirstIterator(graph, currentNode)

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
        val paths = tokens.map {
            pathsFromCurrentNode.value.pointSource.getPath(it.point)
                ?: error("No path between $currentPoint and ${it.point}")
        }

        val shortestPath = paths.minBy { it.length } ?: error("No shortest path?")
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
