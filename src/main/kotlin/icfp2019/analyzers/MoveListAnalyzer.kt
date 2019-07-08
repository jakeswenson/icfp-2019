package icfp2019.analyzers

import icfp2019.core.Analyzer
import icfp2019.model.Action
import icfp2019.model.GameState
import icfp2019.model.Point
import icfp2019.model.RobotId

object MoveListAnalyzer : Analyzer<List<Action>> {
    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> List<Action> {
        val moveAnalyzer = MoveAnalyzer.analyze(initialState)
        return { robotId, gameState ->
            val canMove = moveAnalyzer(robotId, gameState)
            val moves = mutableListOf<Action>()
            fun checkCanDoAction(action: Action) {
                if (canMove(action)) {
                    moves.add(action)
                }
            }
            checkCanDoAction(Action.DoNothing)
            checkCanDoAction(Action.TurnClockwise)
            checkCanDoAction(Action.TurnCounterClockwise)
            checkCanDoAction(Action.Movement.MoveLeft)
            checkCanDoAction(Action.Movement.MoveRight)
            checkCanDoAction(Action.Movement.MoveDown)
            checkCanDoAction(Action.Movement.MoveUp)
            for (location: Point in gameState.teleportDestination) {
                checkCanDoAction(Action.TeleportBack(location))
            }
            checkCanDoAction(Action.CloneRobot)
            checkCanDoAction(Action.AttachFastWheels)
            checkCanDoAction(Action.StartDrill)
            checkCanDoAction(Action.PlantTeleportResetPoint)

            // Need to check add manipulator at allowable points
            moves
        }
    }
}
