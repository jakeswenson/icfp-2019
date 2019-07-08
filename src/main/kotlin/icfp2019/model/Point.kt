package icfp2019.model

import icfp2019.core.GameMap
import icfp2019.core.contains

data class NeighborPoint(private val point: Point, val movement: Action.Movement) {
    val move get() = point.apply()
    val fastMove get() = point.apply().apply()

    private fun Point.apply(): Point = when (movement) {
        Action.Movement.MoveRight -> right()
        Action.Movement.MoveLeft -> left()
        Action.Movement.MoveUp -> up()
        Action.Movement.MoveDown -> down()
    }
}

data class Point(val x: Int, val y: Int) {
    companion object {
        private val origin = Point(0, 0)
        fun origin(): Point = origin
    }

    private val nonNegative = x >= 0 && y >= 0
    fun up(): Point = copy(y = y + 1)
    fun down(): Point = copy(y = y - 1)
    fun left(): Point = copy(x = x - 1)
    fun right(): Point = copy(x = x + 1)
    operator fun plus(offset: Point): Point = copy(x = x + offset.x, y = y + offset.y)

    fun isNeighbor(otherPoint: Point): Boolean = when (otherPoint) {
        left(), right(), up(), down() -> true
        else -> false
    }

    fun actionToGetToNeighbor(neighbor: Point): Action.Movement = when (neighbor) {
        left() -> Action.Movement.MoveLeft
        right() -> Action.Movement.MoveRight
        up() -> Action.Movement.MoveUp
        down() -> Action.Movement.MoveDown
        else -> throw Exception("neighbor was not really a neighbor")
    }

    fun neighbors(): Sequence<NeighborPoint> {
        val point = this
        return sequence {
            yield(NeighborPoint(point, Action.Movement.MoveUp))
            yield(NeighborPoint(point, Action.Movement.MoveDown))
            yield(NeighborPoint(point, Action.Movement.MoveRight))
            yield(NeighborPoint(point, Action.Movement.MoveLeft))
        }
    }

    fun <V> lookupIn(board: GameMap<V>): V = when {
        this in board -> board[x][y]
        else -> error("Point not in board")
    }
}
