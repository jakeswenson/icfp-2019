package icfp2019.analyzers

import icfp2019.model.GameState
import icfp2019.model.RobotId
import icfp2019.printBoard
import icfp2019.toProblem
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GetNumberOfWrappedOrNotTests {
    @Test
    fun testSimple() {

        val map3x2 = """
            .w.
            ww.
        """.toProblem()
        val gameState = GameState(map3x2).initialize()
        printBoard(gameState)

        val columns = gameState.board()
        Assertions.assertEquals(3, columns.size)
        Assertions.assertEquals(2, columns[0].size)
        Assertions.assertEquals(2, columns[1].size)
        Assertions.assertEquals(2, columns[2].size)

        val results = GetNumberOfWrappedOrNot.analyze(gameState)(RobotId.first, gameState)
        Assertions.assertEquals(3, results.wrapped) // initial wrap
        Assertions.assertEquals(3, results.unwrapped)
    }
}
