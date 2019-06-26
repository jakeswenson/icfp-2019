package icfp2019.analyzers

import icfp2019.Cache
import icfp2019.core.Analyzer
import icfp2019.model.*

object ClonePointsAnalyzer : Analyzer<Set<Point>> {
    private val cache = Cache.forBoardState { state ->
        val boosterNodes = state.allStates()
            .filter { it.hasBooster(Booster.CloneToken) || it.hasBooster(Booster.CloningLocation) }
        if (!boosterNodes.any()) boosterNodes.map { it.point }.toSet()
        else emptySet()
    }

    override fun analyze(initialState: GameState): (robotId: RobotId, state: GameState) -> Set<Point> {
        return { _, state -> cache(state.boardState()) }
    }
}
