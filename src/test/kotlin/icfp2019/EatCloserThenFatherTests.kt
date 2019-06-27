package icfp2019

import icfp2019.model.*
import icfp2019.strategies.EatCloserThenFarther
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EatCloserThenFatherTests {
    @Test
    fun testSimple() {
        val problem3Input = loadProblem(3)

        val problem3 = parseDesc(problem3Input, "Test")
        val gameState = GameState(problem3).withRobotPosition(RobotId.first, Point(20, 0))
        val strategy = EatCloserThenFarther.compute(gameState)
        val action = strategy(RobotId.first, gameState)
        Assertions.assertEquals(Action.MoveRight, action)
    }

    @Test
    fun test2() {
        val problem = """
        ...XX
        w@...
        .w.XX
    """.toProblem()
        val gs = GameState(problem)
        val s = EatCloserThenFarther.compute(gs)
        val m1 = s(RobotId.first, gs)
        Assertions.assertEquals(Action.MoveUp, m1)
    }

    @Test
    fun test3() {
        val problem = """
        .w.XX
        w@...
        .w.XX
    """.toProblem()
        val gs = GameState(problem)
        val s = EatCloserThenFarther.compute(gs)
        val m1 = s(RobotId.first, gs)
        Assertions.assertEquals(Action.MoveRight, m1)
    }

    @Test
    fun test4() {
        val problem = """
        .w.XX
        w@w..
        .w.XX
    """.toProblem()
        val gs = GameState(problem)
        val s = EatCloserThenFarther.compute(gs)
        val m1 = s(RobotId.first, gs)
        Assertions.assertEquals(Action.MoveRight, m1)
    }
}
