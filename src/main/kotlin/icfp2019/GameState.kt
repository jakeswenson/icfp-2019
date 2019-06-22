package icfp2019

data class GameState(
    val gameBoard: Array<Short>, // TODO: Martin will replace with GameBoard Builder
    val robotStateList: List<RobotState>,
    val teleportDestination: List<Point>,
    val unusedBoosters: List<Booster>
)
