package icfp2019

data class GameBoard(
    val problem: Problem,
    val cells: Array<Short>,
    val width: Int,
    val height: Int
) {
    companion object {
        fun gameBoardOf(problem: Problem): GameBoard {
            val cells = Array<Short>(problem.size.x * problem.size.y) { 0 }
            for (column in problem.map) {
                for (cell in column) {
                    var flags: Short = 0
                    if (cell.isObstacle) {
                        flags = Cell.setFlag(flags, Cell.OBSTACLE)
                    } else {
                        flags = when (cell.booster) {
                            Booster.ExtraArm -> Cell.setFlag(flags, Cell.BOOST_EXT)
                            Booster.Drill -> Cell.setFlag(flags, Cell.BOOST_DRILL)
                            Booster.FastWheels -> Cell.setFlag(flags, Cell.BOOST_FAST)
                            Booster.Teleporter -> Cell.setFlag(flags, Cell.BOOST_TELEPORT)
                            Booster.CloneToken -> Cell.setFlag(flags, Cell.BOOST_CLONE)
                            else -> flags
                        }
                    }
                    cells[cell.point.x * problem.size.y + cell.point.y] = flags
                }
            }

            return GameBoard(problem, cells, problem.size.x, problem.size.y)
        }
    }

    fun isInBoard(x: Int, y: Int): Boolean {
        return (x > 0 && x < problem.size.x && y > 0 && y < problem.size.y)
    }

    fun get(x: Int, y: Int): Short {
        if (!isInBoard(x, y)) {
            return Cell.NOT_A_CELL
        }
        return cells[x * height + y]
    }

    fun set(x: Int, y: Int, value: Short): GameBoard {
        if (!isInBoard(y, y)) {
            return this
        }
        val newCells = cells.clone()
        newCells[x * height + y] = value
        return GameBoard(problem, newCells, width, height)
    }
}
