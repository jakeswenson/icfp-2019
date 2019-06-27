package icfp2019

import org.junit.jupiter.api.Test

class LongestStraightPathAlgorithmTests {
    @Test
    fun testStraightPath() {
        val problem3Input = loadProblem(3)
        val p = parseDesc(problem3Input, "Test")
        val ret = applyLongestStraightPathAlgorithm(p.map)
        printBoard(ret)
    }

    private fun printBoard(map: List<List<Pair<Direction, Int>>>) {
        println("${map.size} ${map[0].size}")
        val maxX = map.size
        val maxY = map[0].size
        for (y in (maxY - 1) downTo 0) {
            for (x in 0 until maxX) {
                print("%s:%02d".format(map[x][y].first.toString(), map[x][y].second))
                print(' ')
            }
            println()
        }
    }
}
