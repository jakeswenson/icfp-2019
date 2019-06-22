package icfp2019

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
    val workingDir: Path = Paths.get("")

    val solutions = mutableListOf<Solution>()
    readZipFile(File("problems.zip"))
        .filter { it.line.isNotEmpty() }
        .forEach {
            val problem = parseDesc(it)
            val solution = solve(problem)
            encodeSolution(solution, workingDir)
        }

    writeZip(workingDir, solutions)
}

fun writeZip(workingDir: Path, solutions: MutableList<Solution>) {
    TODO(workingDir.toString() + solutions.toString() + "not implemented")
}

fun readZipFile(file: File): List<ProblemParseInput> {
    TODO(file.toString() + "not implemented")
}

/*
Task:
 1. Open Zip file
 2. parse a problem at a time: prob_NNN.desc
 3. solve problem
 4. encode solution
 5. output to file prob_NNN.sol (use checker to validate?) https://icfpcontest2019.github.io/solution_checker/
 6. add solution to another zip (script/program)
 */

fun solve(problem: Problem): Solution {
    return Solution(problem.problemId, listOf())
}

fun constructObstacleMap(problem: Problem): Array<Array<Boolean>> {
    return problem.map.map { it.map { it.isObstacle }.toTypedArray() }.toTypedArray()
}

fun encodeSolution(solution: Solution, directory: Path): File {
    val file = Files.createFile(directory.resolve("prob-${solution.problemId.id}.sol"))
    // TODO
    return file.toFile()
}
