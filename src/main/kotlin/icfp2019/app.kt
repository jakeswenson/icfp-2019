package icfp2019

import icfp2019.model.*
import icfp2019.strategies.DFSStrategy
import java.io.File
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val path = Paths.get(if (args.isNotEmpty()) args[0] else "./problems").toAbsolutePath()
    path.toFile().walk().forEach {
        if (it.isFile && it.extension.equals("desc")) {
            var summary = "Invalid Problem"
            val timeElapsed = measureTimeMillis {
                print("Running ${it.name}... ")
                val problem = parseDesc(it.readText(), it.name)
                val solution = brain(problem, listOf(DFSStrategy), 1)
                File(it.parent, "${it.nameWithoutExtension}.sol").writeText(solution.toString())
                summary = solution.summary()
            }
            val logSummary = "$summary in ${timeElapsed}ms\n"
            print(logSummary)
            File(it.parent, "${it.nameWithoutExtension}.log").appendText(logSummary)
        }
    }
}

fun constructObstacleMap(problem: Problem): Array<Array<Boolean>> {
    return problem.map.map { it.map { it.isObstacle }.toTypedArray() }.toTypedArray()
}
