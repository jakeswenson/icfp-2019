package icfp2019

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
  val workingDir: Path = Paths.get("")

  val solutions = mutableListOf<Solution>()
  readZipFile(File("problems.zip"))
      .filter { it.file.isFile }
      .forEach {
        val problem = parseDesc(it)
        val solution = solve(problem)
        encodeSolution(solution, workingDir)
      }

  writeZip(workingDir, solutions)
}

fun writeZip(workingDir: Path, solutions: MutableList<Solution>) {
  TODO("not implemented")
}

fun readZipFile(file: File): List<ProblemDescription> {
  TODO("not implemented")
}

enum class Boosters {
  B, F, L, X
}

data class Point(val x: Int, val y: Int)
data class Node(val point: Point, val isObstacle: Boolean, val booster: Boosters)

data class ProblemId(val id: Int)
data class ProblemDescription(val problemId: ProblemId, val file: File)
data class Problem(val problemId: ProblemId, val startingPosition: Point, val map: Array<Array<Node>>)

/*
Task:
 1. Open Zip file
 2. parse a problem at a time: prob_NNN.desc
 3. icfp2019.solve problem
 4. encode solution
 5. output to file prob_NNN.sol (use checker to validate?) https://icfpcontest2019.github.io/solution_checker/
 6. add solution to another zip (script/program)
 */

fun parseDesc(file: ProblemDescription): Problem {
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
  return Problem(ProblemId(0), Point(0, 0), arrayOf())
}

/*
A solution for a task
  prob-NNN.desc
is a sequence of actions encoded as a single-line text file named
  prob-NNN.sol
for the corresponding numberNNN.
The actions are encoded as follows:
  action ::=
      W(move up)
      | S(move down)
      | A(move left)
      | D(move right)
      | Z(do nothing)
      | E(turn manipulators 90°clockwise)
      | Q(turn manipulators 90°counterclockwise)
      | B(dx,dy)(attach a new manipulator with relative coordinates(dx,dy))
      | F(attach fast wheels)
      | L(start using a drill)

  solution ::= rep(action)

  A solution isvalid, if it does not force the worker-wrapper to go through the walls and obstacles
  (unless it uses a drill), respects the rules of using boosters, and, upon finishing,
  leaves all reachablesquares of the map wrapped.
 */

enum class Actions {
  W, S, A, D, Z, E, Q, B, F, L
}

data class Solution(val problemId: ProblemId, val actions: List<Actions>)

fun solve(problem: Problem): Solution {
  return Solution(problem.problemId, listOf())
}

fun encodeSolution(solution: Solution, directory: Path): File {
  val file = Files.createFile(directory.resolve("prob-${solution.problemId.id}.sol"))
  // TODO
  return file.toFile()
}
