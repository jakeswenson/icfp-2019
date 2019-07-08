package icfp2019.analyzers

import icfp2019.core.Analyzer
import icfp2019.model.*

object MoveAnalyzer : Analyzer<(Action) -> Boolean> {
    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> (Action) -> Boolean {
        return { robotId, gameState ->
            { action ->
                var possible = false

                val robotState = gameState.robot(robotId)
                if (initialState.isInBoard(robotState.currentPosition)
                ) {
                    val cell = initialState.get(robotState.currentPosition)

                    fun canMoveTo(point: Point): Boolean {
                        return initialState.isInBoard(point) &&
                                (!cell.isObstacle || robotState.hasActiveDrill())
                    }

                    fun canTeleportTo(point: Point): Boolean {
                        return gameState.teleportDestination.contains(point)
                    }

                    possible = when (action) {
                        Action.DoNothing, Action.TurnClockwise, Action.TurnCounterClockwise -> true
                        Action.Movement.MoveUp -> canMoveTo(robotState.currentPosition.up())
                        Action.Movement.MoveDown -> canMoveTo(robotState.currentPosition.down())
                        Action.Movement.MoveLeft -> canMoveTo(robotState.currentPosition.left())
                        Action.Movement.MoveRight -> canMoveTo(robotState.currentPosition.right())
                        is Action.AttachManipulator -> gameState.backpackContains(Booster.ExtraArm)
                        Action.AttachFastWheels -> gameState.backpackContains(Booster.FastWheels)
                        Action.StartDrill -> gameState.backpackContains(Booster.Drill)
                        Action.PlantTeleportResetPoint -> gameState.backpackContains(Booster.Teleporter)
                        is Action.TeleportBack -> canTeleportTo(action.targetResetPoint)
                        Action.CloneRobot -> gameState.backpackContains(Booster.CloneToken) &&
                                initialState.nodeState(robotState.currentPosition).hasBooster(Booster.CloningLocation)
                    }
                }
                possible
            }
        }
    }
}
