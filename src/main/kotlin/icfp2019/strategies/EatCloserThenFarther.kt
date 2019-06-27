package icfp2019.strategies

import icfp2019.analyzers.Distance
import icfp2019.analyzers.DistanceToWalls
import icfp2019.core.Strategy
import icfp2019.core.applyAction
import icfp2019.model.*

object EatCloserThenFarther : Strategy {
    override fun compute(initialState: GameState): (robotId: RobotId, state: GameState) -> Action {
        val distanceToWallsAnalyzer = DistanceToWalls.analyze(initialState)
        return { robotId, state ->
            // val currentDistance = distanceToWallsAnalyzer(0, state)

            data class Move(val action: Action, val location: Point)
            data class MoveOption(val id: Int, val move: Move)
            data class AppliedMove(val id: Int, val gameState: GameState)

            val allMoves = listOf(
                MoveOption(0, Move(Action.MoveUp, state.robot(robotId).currentPosition.up())),
                MoveOption(1, Move(Action.MoveRight, state.robot(robotId).currentPosition.right())),
                MoveOption(2, Move(Action.MoveDown, state.robot(robotId).currentPosition.down())),
                MoveOption(3, Move(Action.MoveLeft, state.robot(robotId).currentPosition.left()))
            )

            val unWrappedCells = state.boardState().allStates().filter { it.isWrapped.not() }.map { it.point }.toSet()

            // [Index, GameState]
            val movesWithinGameboard = allMoves
                .filter {
                    state.isInBoard(it.move.location) &&
                            (state.get(it.move.location).isObstacle.not() || state.robot(robotId).hasActiveDrill())
                }
                .map { AppliedMove(it.id, applyAction(state, robotId, it.move.action)) }

            data class MoveDistanceToWall(val id: Int, val distance: Distance)

            val movesAvoidingObstacles = movesWithinGameboard
                .map { MoveDistanceToWall(it.id, distanceToWallsAnalyzer(robotId, it.gameState)) }
                .filter { it.distance != DistanceToWalls.obstacleIdentifier }

            // Deal with wrapped vs unwrapped. If all wrapped, go for the largest.
            // Else, go for the smallest.
            val allWrapped = movesAvoidingObstacles
                .map { it.id }
                .none { state.get(allMoves[it].move.location).point in unWrappedCells }

            val result = if (allWrapped) {
                movesAvoidingObstacles.maxBy { it.distance }
            } else {
                movesAvoidingObstacles
                    .filter { state.get(allMoves[it.id].move.location).point in unWrappedCells }
                    .minBy { it.distance }
            }

            if (result != null) {
                allMoves[result.id].move.action
            } else {
                Action.DoNothing
            }
        }
    }
}
