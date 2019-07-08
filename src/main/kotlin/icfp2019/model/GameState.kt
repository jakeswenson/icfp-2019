package icfp2019.model

import icfp2019.core.*
import java.util.*

data class BoardCell(
    val point: Point,
    val isObstacle: Boolean = false,
    val hasTeleporterPlanted: Boolean = false
) {
    constructor(node: Node) : this(node.point, node.isObstacle, node.hasTeleporterPlanted)
}

data class BoardNodeState(
    val point: Point,
    val isWrapped: Boolean = false,
    val booster: Booster? = null
) {
    constructor(node: Node) : this(node.point, node.isWrapped, node.booster)

    val hasBooster: Boolean = booster != null

    fun hasBooster(booster: Booster): Boolean {
        return this.booster == booster
    }
}

typealias Board = GameMap<BoardCell>

fun Board.allCells(): Sequence<BoardCell> = this.asSequence().flatten().map { it!! }
fun Board.nonObstacles(): Sequence<BoardCell> = allCells().filter { !it.isObstacle }

typealias BoardStates = GameMap<BoardNodeState>

fun BoardStates.allStates(): Sequence<BoardNodeState> = this.asSequence().flatten().map { it!! }
fun BoardStates.onlyUnwrapped(): Sequence<BoardNodeState> = allStates().filter { !it.isWrapped }

data class GameState private constructor(
    private val board: Board,
    private val boardState: BoardStates,
    val mapSize: MapSize,
    val startingPoint: Point,
    private val robotStates: Map<RobotId, RobotState> = initialRobotMap(startingPoint),
    val teleportDestination: Set<Point> = emptySet(),
    val unusedBoosters: Map<Booster, Int> = emptyMap()
) {

    constructor(problem: Problem) : this(
        initBoardNodes(problem.map),
        initBoardNodeState(problem),
        problem.size,
        problem.startingPosition
    )

    companion object {
        private fun initialRobotMap(point: Point) = mapOf(RobotId.first to RobotState(RobotId.first, point))

        private fun initBoardNodes(mapCells: MapCells): Board {
            return mapCells.mapNodes { BoardCell(it) }
        }

        private fun initBoardNodeState(problem: Problem): BoardStates {
            return problem.map.mapNodes {
                BoardNodeState(it)
            }
        }
    }

    fun initialize(): GameState {
        return wrapAffectedCells(RobotId.first)
    }

    fun board(): Board = board
    fun boardState(): BoardStates = boardState

    fun get(point: Point): BoardCell = when {
        isInBoard(point) -> board[point]
        else -> error("Access out of game board: $point")
    }

    fun nodeState(point: Point): BoardNodeState = when {
        isInBoard(point) -> boardState[point]
        else -> error("$point Not in board")
    }

    val allRobotIds: SortedSet<RobotId> get() = robotStates.keys.toSortedSet(compareBy { it.id })
    val allRobots: List<RobotState> get() = robotStates.values.toList()

    fun isGameComplete(): Boolean {
        return board().nonObstacles().all { nodeState(it.point).isWrapped }
    }

    fun isInBoard(point: Point): Boolean {
        return (point.x in 0 until mapSize.x && point.y in 0 until mapSize.y)
    }

    fun backpackContains(booster: Booster): Boolean {
        return unusedBoosters.getOrDefault(booster, 0) > 0
    }

    fun updateBoard(point: Point, value: BoardCell): GameState {
        if (!isInBoard(point)) {
            throw ArrayIndexOutOfBoundsException("Access out of game board")
        }
        return copy(board = board.update(point) { value })
    }

    fun updateState(point: Point, value: BoardNodeState): GameState {
        if (!isInBoard(point)) {
            throw ArrayIndexOutOfBoundsException("Access out of game board")
        }
        val newCells = boardState.update(point) { value }
        return copy(boardState = newCells)
    }

    fun withNewRobot(currentPosition: Point): GameState {
        val newId = this.allRobotIds.maxBy { it.id }!!.nextId()
        return withRobotState(newId, RobotState(newId, currentPosition))
    }

    fun withRobotPosition(robotId: RobotId, point: Point): GameState {
        return withRobotState(robotId, robot(robotId).copy(currentPosition = point))
    }

    fun withRobotState(robotId: RobotId, robotState: RobotState): GameState {
        return copy(robotStates = robotStates + (robotId to robotState))
    }

    fun robot(robotId: RobotId): RobotState {
        return this.robotStates.getValue(robotId)
    }

    fun boostersAvailable(booster: Booster): Int {
        return this.unusedBoosters.getOrDefault(booster, 0)
    }

    fun robotIsOn(robotId: RobotId, cloningLocation: Booster.CloningLocation): Boolean {
        return nodeState(robot(robotId).currentPosition).hasBooster(cloningLocation)
    }
}
