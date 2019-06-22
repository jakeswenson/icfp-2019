package icfp2019

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.collect.Range
import com.google.common.collect.TreeRangeSet

val matcher = CharMatcher.anyOf("()")!!

fun parsePoint(mapEdges: String): Point {
    return parseEdges(mapEdges)[0]
}

fun parseEdges(mapEdges: String): List<Point> {
    return mapEdges.split(',')
        .map { matcher.trimFrom(it) }
        .windowed(2, step = 2)
        .map { Point(Integer.parseInt(it[0]), Integer.parseInt(it[1])) }
}

fun parseDesc(problem: ProblemParseInput): Problem {

    val (mapEdges, startPosition, obstacles, boosters) = problem.line.split('#')
    val startPoint = parsePoint(startPosition)
    val verticies = parseEdges(mapEdges)
    val obstacleEdges = parseEdges(obstacles)
    val parsedBosters = parseBoosters(boosters)

    val maxY = verticies.maxBy { it.y }?.y ?: throw RuntimeException()
    val maxX = verticies.maxBy { it.x }?.x ?: throw RuntimeException()

    val xArrayIndices = 0.until(maxX)
    val yArrayIndices = 0.until(maxY)

    val grid =
        xArrayIndices.map { x ->
            yArrayIndices.map { y ->
                Node(Point(x, y), isObstacle = true)
            }.toTypedArray()
        }.toTypedArray()

    val xEdgeGroups = (verticies + obstacleEdges).groupBy { it.x }.mapValues { entry ->
        val set: TreeRangeSet<Int> = TreeRangeSet.create()
        val points: List<Point> = entry.value
        val sortedBy = points.map { it.y }.sortedBy { it }
        sortedBy.windowed(2, step = 2).forEach {
            val (y1, y2) = it
            set.add(Range.closed(y1, y2))
        }

        set
    }

    println(xEdgeGroups)

    yArrayIndices.forEach { y ->
        var inObstacle = true
        xArrayIndices.forEach { x ->
            val yEdges = xEdgeGroups[x]
            val column = grid[x]

            if (yEdges?.encloses(Range.closed(y, y + 1)) == true) {
                inObstacle = !inObstacle
            }

            column[y] = column[y].copy(isObstacle = inObstacle)
        }
    }

    parsedBosters.forEach {
        grid[it.second.x][it.second.y] = grid[it.second.x][it.second.y].copy(booster = it.first)
    }

    // Read lines
    /*
    1. Read lines
    2. Parse map
    Grammar:
      x,y: Nat
                point ::= (x,y)
                  map ::= repSep(point,”,”)
          BoosterCode ::= B|F|L|X
      boosterLocation ::= BoosterCode point
            obstacles ::= repSep(map,”; ”)
             boosters ::= repSep(boosterLocation,”; ”)
                 task ::= map # point # obstacles # boosters
     */
    return Problem(problem.problemId, Size(maxX, maxY), startPoint, grid)
}

fun parseBoosters(boosters: String): List<Pair<Booster, Point>> {
    return Splitter.on(';')
        .omitEmptyStrings()
        .split(boosters)
        .map {
            Booster.fromString(it[0]) to parsePoint(it.substring(1))
        }
}
