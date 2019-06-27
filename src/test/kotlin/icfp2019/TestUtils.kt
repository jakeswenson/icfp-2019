package icfp2019

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import icfp2019.model.*
import org.junit.jupiter.api.Assertions
import org.pcollections.PVector
import org.pcollections.TreePVector
import java.nio.file.Paths

fun String.toProblem(): Problem {
    return parseTestMap(this)
}

fun String.toMultipleProblems(): List<Problem> {
    val splitter = Splitter.on(CharMatcher.anyOf("-=")).omitEmptyStrings()
    return splitter.splitToList(this).map { it.toProblem() }
}

fun String.toMultipleProblemsAndExpectations(): List<Pair<Problem, Problem>> {
    return this.toMultipleProblems().windowed(size = 2, step = 2)
        .map { (problem, expectation) -> problem to expectation }
}

fun GameState.toProblem(): Problem {
    val nodes = this.board().map {
        it.map { cell ->
            val state = this.nodeState(cell.point)
            Node(
                cell.point,
                isObstacle = cell.isObstacle,
                isWrapped = state.isWrapped,
                hasTeleporterPlanted = cell.hasTeleporterPlanted,
                booster = state.booster
            )
        }
    }

    val grid = TreePVector.from<PVector<Node>>(nodes.map { TreePVector.from(it) })

    return Problem(
        name = "result",
        size = this.mapSize,
        startingPosition = this.startingPoint,
        map = grid
    )
}

fun loadProblem(problemNumber: Int): String {
    val path = Paths.get("problems/prob-${problemNumber.toString().padStart(3, padChar = '0')}.desc").toAbsolutePath()
    return path.toFile().readText()
}

fun boardString(problem: Problem, path: Set<Point> = setOf()): String =
    boardString(problem.map, problem.size, problem.startingPosition, path)

fun boardString(cells: List<List<Node>>, size: MapSize, startingPosition: Point, path: Set<Point> = setOf()): String {
    val lines = mutableListOf<String>()
    for (y in (size.y - 1) downTo 0) {
        val row = boardRowString(size, cells, y, path, startingPosition)
        lines.add(row)
    }
    return lines.joinToString(separator = "\n")
}

private fun boardRowString(
    size: MapSize,
    cells: List<List<Node>>,
    y: Int,
    path: Set<Point>,
    startingPosition: Point
): String {
    return (0 until size.x).map { x ->
        val node = cells[x][y]
        when {
            node.hasTeleporterPlanted -> '*'
            node.point in path -> '|'
            node.isWrapped -> 'w'
            startingPosition == Point(x, y) -> '@'
            node.isObstacle -> 'X'
            node.booster != null -> 'o'
            else -> '.'
        }
    }.joinToString(separator = " ")
}

fun boardComparisonString(
    size: MapSize,
    startingPosition: Point,
    path: Set<Point> = setOf(),
    left: List<List<Node>>,
    right: List<List<Node>>
): String {
    val lines = mutableListOf<String>()
    for (y in (size.y - 1) downTo 0) {
        val leftRow = boardRowString(size, left, y, path, startingPosition)
        val rightRow = boardRowString(size, right, y, path, startingPosition)
        lines.add(leftRow.padEnd(size.x * 2 + 5, ' ') + rightRow)
    }
    return lines.joinToString(separator = "\n")
}

fun printBoardComparison(expected: Problem, result: Problem, path: Set<Point> = setOf()) {
    println("${expected.size}")
    println("Expected".padEnd(expected.size.x*2 + 5, ' ') + "Result")
    print(boardComparisonString(expected.size, expected.startingPosition, path, expected.map, result.map))
    println()
}

fun GameState.assertEquals(expected: Problem) {
    val result = this.toProblem()
    if (expected.map != result.map) {
        printBoardComparison(expected, result)
        Assertions.fail<Any>("Boards are not equal")
    }
}

fun printBoard(p: Problem, path: Set<Point> = setOf()) {
    println("${p.size}")
    print(boardString(p.map, p.size, p.startingPosition, path))
    println()
}

fun printBoard(state: GameState, path: Set<Point> = setOf()) {
    printBoard(state.toProblem(), path)
}

fun parseTestMap(map: String): Problem {
    val mapLineSplitter = Splitter.on(CharMatcher.anyOf("\r\n")).omitEmptyStrings()
    val lines = mapLineSplitter.splitToList(map)
        .map { CharMatcher.whitespace().removeFrom(it) }
        .filter { it.isBlank().not() }
        .reversed()
    val height = lines.size
    val width = lines[0].length
    if (lines.any { it.length != width }) throw IllegalArgumentException("Inconsistent map line lengths")
    val startPoint =
        (0 until width).map { x ->
            (0 until height).map { y ->
                if (lines[y][x] == '@') Point(x, y)
                else null
            }
        }.flatten().find { it != null } ?: Point.origin()
    return Problem("Test", MapSize(width, height), startPoint, TreePVector.from((0 until width).map { x ->
        TreePVector.from((0 until height).map { y ->
            val point = Point(x, y)
            when (val char = lines[y][x]) {
                'X' -> Node(point, isObstacle = true)
                'w' -> Node(point, isObstacle = false, isWrapped = true)
                '.' -> Node(point, isObstacle = false)
                '@' -> Node(point, isObstacle = false)
                '*' -> Node(point, isObstacle = false, hasTeleporterPlanted = true, isWrapped = true)
                in Booster.parseChars -> Node(point, isObstacle = false, booster = Booster.fromChar(char))
                else -> throw IllegalArgumentException("Unknown Char '$char'")
            }
        })
    }))
}
