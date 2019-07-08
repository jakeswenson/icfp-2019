package icfp2019

import icfp2019.core.Strategy
import icfp2019.model.Action
import icfp2019.model.GameState
import icfp2019.model.RobotId
import org.junit.jupiter.api.Test
import java.util.*

internal class BrainKtTest {
    class TestStrategy(vararg actions: Action) : Strategy {
        private val queue = ArrayDeque(actions.toList())

        override fun compute(initialState: GameState): (robotId: RobotId, state: GameState) -> Action {
            return { _, _ ->
                if (queue.isEmpty()) {
                    Action.DoNothing
                } else {
                    queue.pop()
                }
            }
        }
    }

    @Test
    fun brainStepTest() {
        val problem = parseTestMap(init)
        val solution = parseTestMap(fini)
        printBoard(problem)
        val strategy = TestStrategy(Action.Movement.MoveDown, Action.Movement.MoveRight)
        val initialState = GameState(problem).initialize()
        var state = initialState
        for (i in 0..1) {
            val (result, actions) = brainStep(
                initialState,
                state,
                strategy,
                1
            )

            state = result

            println(actions)
            printBoard(result)
        }

        state.assertEquals(solution)
    }

    private val init =
        """
        @...
        ....
        ....
        ....
        """

    private val fini =
        """
        www.
        www.
        .ww.
        ....
        """
}
