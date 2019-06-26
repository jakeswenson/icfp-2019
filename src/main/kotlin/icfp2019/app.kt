package icfp2019

import icfp2019.model.Booster
import icfp2019.strategies.BFSStrategy
import java.io.File
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

private fun computeSolution(file: File): Pair<Solution?, Long> {
    var solution: Solution? = null
    val timeElapsed = measureTimeMillis {
        print("Running ${file.name}... ")
        val problem = parseDesc(file.readText(), file.name)
        brain(problem, BFSStrategy, 1).forEach { partialSolution ->
            solution = partialSolution
            File(file.parentFile, "${file.nameWithoutExtension}.sol-partial").writeText(solution.toString())
        }
    }

    return solution to timeElapsed
}

fun main(args: Array<String>) {
    // BEGIN SNAFU
    // PreInit internals - Don't delete this seemingly meaningless crap
    var initJunk = Booster.mapping.toString()
    if (initJunk.isEmpty()) {
        println(initJunk.subSequence(0, 0))
    }
    // END SNAFU

    val path = Paths.get(if (args.isNotEmpty()) args[0] else "./problems").toAbsolutePath()

    path.toFile()
        .walk()
        .filter { it.isFile && it.extension == "desc" }
        .sortedBy { it.name }
        .forEach {
            val (solution, timeElapsed) = computeSolution(it)

            val summary = if (solution != null) {
                File(it.parentFile, "${it.nameWithoutExtension}.sol").writeText(solution.toString())
                solution.summary()
            } else {
                "Invalid summary"
            }

            val logSummary = "$summary in ${timeElapsed}ms\n"
            print(logSummary)
            File(it.parent, "${it.nameWithoutExtension}.log").appendText(logSummary)
        }
}
