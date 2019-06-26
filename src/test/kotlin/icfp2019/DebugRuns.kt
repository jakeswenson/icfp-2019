package icfp2019

import icfp2019.core.applyAction
import icfp2019.model.Action
import icfp2019.model.GameState
import icfp2019.model.Point
import icfp2019.model.RobotId
import icfp2019.strategies.BFSStrategy
import org.junit.jupiter.api.Test

class Tests {
    @Test
    fun run() {
        val map = """
            X X X X X X X . . . . . . . . 
            X X X X X X X X X . . . . c . 
            X X X X X X X X X X . . . . . 
            X X X X . . X X X . . . . . . 
            X X . . . . . X X . . . X . X 
            X . . . . . . . . . . X X X X 
            . . . . . . . . . . . . X . X 
            . . . . . . . . . . . . . . . 
            . . . . . . . . . . . x . . X 
            @ . . . X X . . . . . . . . . 
            X . . . X . . X . . . . . . X 
            X X . . X . . X . . . . . . . 
            X X X . . . . X . . . . . . . 
            X X X X . . . X . . . . . . . 
            X X X X X X . . . . . . . . . 
        """.toProblem()

        val startingWalkState = GameState(map)
        val bfs = BFSStrategy.compute(startingWalkState)

        val result: Pair<GameState, List<Pair<Action, Point>>> =
            generateSequence(startingWalkState to emptyList<Pair<Action, Point>>()) { (state, actions) ->
                if (state.isGameComplete()) null
                else {
                    val action = bfs(RobotId.first, state)
                    val newState = applyAction(state, RobotId.first, action)
                    newState to actions.plus(action to newState.robot(RobotId.first).currentPosition)
                }
            }.last()

        println(result.second)
        printBoard(result.first, result.second.map { it.second }.toSet())
    }
}
