package icfp2019.model

sealed class Action {
    fun toSolutionString(): String = when (this) {
        Movement.MoveUp -> "W"
        Movement.MoveDown -> "S"
        Movement.MoveLeft -> "A"
        Movement.MoveRight -> "D"
        DoNothing -> "Z"
        TurnClockwise -> "E"
        TurnCounterClockwise -> "Q"
        AttachFastWheels -> "F"
        StartDrill -> "L"
        PlantTeleportResetPoint -> "R"
        CloneRobot -> "C"
        is AttachManipulator -> "B(${this.point.x},${this.point.y})"
        is TeleportBack -> "T(${this.targetResetPoint.x},${this.targetResetPoint.y})"
    }

    override fun toString(): String = when (this) {
        Movement.MoveUp -> "MoveUp"
        Movement.MoveDown -> "MoveDown"
        Movement.MoveLeft -> "MoveLeft"
        Movement.MoveRight -> "MoveRight"
        DoNothing -> "DoNothing"
        TurnClockwise -> "TurnClockwise"
        TurnCounterClockwise -> "TurnCounterClockwise"
        AttachFastWheels -> "AttachFastWheels"
        StartDrill -> "StartDrill"
        PlantTeleportResetPoint -> "PlantTeleportResetPoint"
        CloneRobot -> "CloneRobot"
        is AttachManipulator -> "AttachManipulator(${this.point})"
        is TeleportBack -> "TeleportBack(${this.targetResetPoint})"
    }

    sealed class Movement : Action() {
        object MoveUp : Movement()
        object MoveDown : Movement()
        object MoveLeft : Movement()
        object MoveRight : Movement()
    }

    object DoNothing : Action()
    object TurnClockwise : Action()
    object TurnCounterClockwise : Action()
    data class AttachManipulator(val point: Point) : Action()
    object AttachFastWheels : Action()
    object StartDrill : Action()
    object PlantTeleportResetPoint : Action()
    data class TeleportBack(val targetResetPoint: Point) : Action()
    object CloneRobot : Action()
}
