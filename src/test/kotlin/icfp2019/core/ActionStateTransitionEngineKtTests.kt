package icfp2019.core

import com.google.common.io.Resources
import icfp2019.*
import icfp2019.model.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.streams.asStream

internal class ActionStateTransitionEngineKtTests {

    @Test
    fun verifyMovements() {
        val problem = """
                        ..
                        @.
                    """.toProblem()
        val startingPosition = problem.startingPosition
        val startingState = GameState(problem).initialize()
        val upRightState = applyAction(startingState, RobotId.first, Action.MoveUp).let {
            applyAction(it, RobotId.first, Action.MoveRight)
        }

        Assertions.assertEquals(
            startingPosition.up().right(),
            upRightState.robot(RobotId.first).currentPosition
        )

        val backToOrigin = applyAction(upRightState, RobotId.first, Action.MoveDown).let {
            applyAction(it, RobotId.first, Action.MoveLeft)
        }

        Assertions.assertEquals(
            startingPosition,
            backToOrigin.robot(RobotId.first).currentPosition
        )
    }

    @Test
    fun verifyPickupBooster() {

        val problem = "l@".toProblem()
        val gameState = GameState(problem).initialize()

        Assertions.assertEquals(
            BoardNodeState(Point.origin(), isWrapped = false, booster = Booster.Drill),
            gameState.nodeState(Point.origin())
        )

        listOf(Action.MoveLeft).applyTo(gameState).let {
            Assertions.assertEquals(mapOf(Booster.Drill to 1), it.unusedBoosters)
            Assertions.assertEquals(
                BoardNodeState(Point.origin(), isWrapped = true),
                it.nodeState(Point.origin())
            )
        }
    }

    @Test
    fun verifyArmsAttach() {

        val problem = "b@".toProblem()
        val gameState = GameState(problem).initialize()

        Assertions.assertEquals(
            BoardNodeState(Point.origin(), booster = Booster.ExtraArm),
            gameState.nodeState(Point.origin())
        )

        val pickupState = listOf(Action.MoveLeft).applyTo(gameState)
        pickupState.run {
            Assertions.assertEquals(1, boostersAvailable(Booster.ExtraArm))
            Assertions.assertEquals(mapOf(Booster.ExtraArm to 1), this.unusedBoosters)
            Assertions.assertEquals(
                BoardNodeState(Point.origin(), isWrapped = true, booster = null),
                this.nodeState(Point.origin())
            )
        }

        val applyTo = listOf(Action.AttachManipulator(Point(2, 0))).applyTo(pickupState)
        applyTo.run {
            Assertions.assertEquals(0, boostersAvailable(Booster.ExtraArm))
            val robot = robot(RobotId.first)
            Assertions.assertEquals(
                listOf(Point(1, 0), Point(1, 1), Point(1, -1), Point(2, 0)),
                robot.armRelativePoints
            )
        }
    }

    @Test
    fun verifyFastMove() {

        val problem = """
        ...XX
        f....
        @..XX
    """.toProblem()
        val gameState = GameState(problem).initialize()

        val actions = listOf(
            Action.MoveUp, Action.AttachFastWheels, Action.MoveRight, Action.MoveRight
        )
        val expectedProblem = """
        .wwXX
        wwwww
        wwwXX
    """.toProblem()

        actions.applyTo(gameState).assertEquals(expectedProblem)
    }

    @Test
    fun verifyDrill() {

        val problem = """
        ..X..
        @lX..
        ..X..
    """.toProblem()
        val gameState = GameState(problem).initialize()

        val actions = listOf(
            Action.MoveRight, Action.StartDrill, Action.MoveRight, Action.MoveRight, Action.MoveRight
        )
        val expectedProblem = """
        .wXww
        wwwww
        .wXww
    """.toProblem()

        actions.applyTo(gameState).assertEquals(expectedProblem)
    }

    @Test
    fun verifyTeleport() {

        val problem = """
        .....
        r....
        @....
    """.toProblem()
        val gameState = GameState(problem)

        val actions = listOf(
            Action.MoveUp, Action.MoveUp, Action.MoveRight, Action.MoveRight,
            Action.MoveRight, Action.MoveRight, Action.PlantTeleportResetPoint,
            Action.MoveDown, Action.MoveDown, Action.MoveLeft, Action.MoveLeft,
            Action.TeleportBack(Point(4, 2)), Action.MoveLeft, Action.MoveDown,
            Action.MoveLeft, Action.MoveLeft, Action.MoveDown
        )
        val expectedProblem = """
        wwww*
        wwwww
        @wwww
    """.toProblem()

        actions.applyTo(gameState).let { state ->
            state.assertEquals(expectedProblem)
        }
    }

    @Test
    fun verifyWrapping() {

        val problem = """
        ...XX
        .....
        @..XX
    """.toProblem()
        val gameState = GameState(problem)

        val actions = listOf(
            Action.MoveUp, Action.MoveUp,
            Action.MoveRight, Action.MoveDown, Action.MoveDown,
            Action.MoveRight, Action.MoveUp, Action.MoveUp,
            Action.MoveDown
        )
        val expectedProblem = """
        wwwXX
        wwww.
        @wwXX
    """.toProblem()

        actions.applyTo(gameState).let {
            it.assertEquals(expectedProblem)
        }
    }

    companion object {
        @JvmStatic
        @Suppress("unused", "UnstableApiUsage")
        fun longArmsUpSource(): Stream<Arguments> {

            return sequence {
                val problems = Resources.getResource(
                    ActionStateTransitionEngineKtTests::class.java,
                    "longArmsUp.problems"
                ).readText().toMultipleProblemsAndExpectations()

                yieldAll(problems.map { (problem, expectation) ->
                    Arguments.of(
                        makeLongArmedRobot(problem),
                        expectation
                    )
                })
            }.asStream()
        }

        @JvmStatic
        @Suppress("unused", "UnstableApiUsage")
        fun longArmsDownSource(): Stream<Arguments> {

            return sequence {
                val problems = Resources.getResource(
                    ActionStateTransitionEngineKtTests::class.java,
                    "longArmsDown.problems"
                ).readText().toMultipleProblemsAndExpectations()

                yieldAll(problems.map { (problem, expectation) ->
                    Arguments.of(
                        makeLongArmedRobot(problem, down = true),
                        expectation
                    )
                })
            }.asStream()
        }

        private fun makeLongArmedRobot(problem: Problem, down: Boolean = false): GameState {
            val originalGameState = GameState(problem)
            val originalRobotState = originalGameState.robot(RobotId.first)
            val newRobotState = originalRobotState.copy(
                armRelativePoints = originalRobotState.armRelativePoints.plus(
                    (2..10).map { Point(1, if (down) -it else it) }
                )
            )
            return originalGameState.withRobotState(RobotId.first, newRobotState).initialize()
        }

        private fun makeRobotWithBalancedArms(problem: Problem): GameState {
            val originalGameState = GameState(problem)
            val originalRobotState = originalGameState.robot(RobotId.first)
            val newRobotState = originalRobotState.copy(
                armRelativePoints = originalRobotState.armRelativePoints.plus(
                    (2..5).flatMap { listOf(Point(1, it), Point(1, -it)) }
                )
            )
            val gameState = originalGameState.withRobotState(RobotId.first, newRobotState).initialize()
            return gameState
        }
    }

    @ParameterizedTest
    @MethodSource(value = ["longArmsUpSource", "longArmsDownSource"])
    fun verifyLongArm(testCase: GameState, expected: Problem) {
        applyAction(testCase, RobotId.first, Action.MoveRight).assertEquals(expected)
    }

    @Test
    fun verifyBoth() {

        val problem = """
        .....
        .....
        .....
        .....
        .....
        .....
        @....
        .....
        .....
        .....
        .....
        .....
        .....
    """.toProblem()
        val gameState = makeRobotWithBalancedArms(problem)

        val actions = listOf(
            Action.MoveRight
        )
        val expectedProblem = """
        .....
        .ww..
        .ww..
        .ww..
        .ww..
        .ww..
        www..
        .ww..
        .ww..
        .ww..
        .ww..
        .ww..
        .....
    """.toProblem()

        actions.applyTo(gameState).assertEquals(expectedProblem)
    }

    @Test
    fun verifyBoth1() {

        val problem = """
        .....
        .....
        .....
        .....
        .X...
        .....
        @....
        .X...
        .....
        .....
        .....
        .....
        .....
    """.toProblem()
        val gameState = makeRobotWithBalancedArms(problem)

        val actions = listOf(
            Action.MoveRight
        )
        val expectedProblem = """
        .....
        .w...
        .....
        ..w..
        .Xw..
        .ww..
        www..
        .Xw..
        .....
        .w...
        .w...
        .w...
        .....
    """.toProblem()

        actions.applyTo(gameState).assertEquals(expectedProblem)
    }

    @Test
    fun verifyLongArmX2() {

        val problem = """
        .....
        .....
        .....
        .....
        .....
        .....
        .....
        .....
        .....
        ..X..
        .....
        @....
    """.toProblem()
        val gameState = makeLongArmedRobot(problem)

        val actions = listOf(
            Action.MoveRight
        )
        val expectedProblem = """
        .....
        .ww..
        .ww..
        .ww..
        .ww..
        .ww..
        .ww..
        .w...
        .w...
        .wX..
        .ww..
        www..
    """.toProblem()

        actions.applyTo(gameState).assertEquals(expectedProblem)
    }

    @Test
    fun verifyLongArmX3() {

        val problem = """
        .....
        .....
        .....
        .....
        .....
        .....
        .....
        .....
        ..X..
        .....
        .....
        @....
    """.toProblem()
        val gameState = makeLongArmedRobot(problem)

        val actions = listOf(
            Action.MoveRight
        )
        val expectedProblem = """
        .....
        .ww..
        .ww..
        .ww..
        .ww..
        .w...
        .w...
        .w...
        .wX..
        .ww..
        .ww..
        www..
    """.toProblem()

        actions.applyTo(gameState).assertEquals(expectedProblem)
    }

    @Test
    fun verifyBothX1() {

        val problem = """
        .....
        .....
        .....
        .....
        ..X..
        .....
        @....
        ..X..
        .....
        .....
        .....
        .....
        .....
    """.toProblem()
        val gameState = makeRobotWithBalancedArms(problem)

        val actions = listOf(
            Action.MoveRight
        )
        val expectedProblem = """
        .....
        .ww..
        .w...
        .w...
        .wX..
        .ww..
        www..
        .wX..
        .w...
        .ww..
        .ww..
        .ww..
        .....
    """.toProblem()

        actions.applyTo(gameState).assertEquals(expectedProblem)
    }

    private fun List<Action>.applyTo(gameState: GameState): GameState {
        return this.fold(gameState) { state, action ->
            applyAction(state, RobotId.first, action)
        }
    }
}
