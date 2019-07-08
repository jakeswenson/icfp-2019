package icfp2019

import icfp2019.core.DistanceEstimate
import icfp2019.core.Strategy
import icfp2019.core.applyAction
import icfp2019.core.pickupBoosterIfAvailable
import icfp2019.model.Action
import icfp2019.model.GameState
import icfp2019.model.Problem
import icfp2019.model.RobotId

data class StrategyResult(val nextState: GameState, val nextAction: Action)

fun strategySequence(
    robotId: RobotId,
    strategy: (robotId: RobotId, state: GameState) -> Action,
    currentState: GameState,
    initialAction: Action = Action.DoNothing
): Sequence<StrategyResult> {
    return generateSequence(
        seed = StrategyResult(currentState, initialAction),
        nextFunction = { (gameState, _) ->
            if (gameState.isGameComplete()) null
            else {
                val gameStateWithPickedupItems = gameState.pickupBoosterIfAvailable(robotId)
                val nextAction = strategy(robotId, gameStateWithPickedupItems)
                val nextState = applyAction(gameStateWithPickedupItems, robotId, nextAction)
                StrategyResult(nextState, nextAction)
            }
        }
    ).drop(1) // skip the initial state
}

data class BrainScore(
    val robotId: RobotId,
    val strategyResult: StrategyResult,
    val distanceEstimate: DistanceEstimate,
    val strategy: Strategy,
    val simulationFinalState: GameState
)

fun Sequence<StrategyResult>.score(
    robotId: RobotId,
    strategy: Strategy
): BrainScore {
    // grab the first and last game state in this simulation path
    data class ReduceState(val strategyResult: StrategyResult, val finalState: GameState)

    val result =
        map { ReduceState(it, it.nextState) }.reduce { (initial, _), (_, final) -> ReduceState(initial, final) }

    // return the initial game state, if this path is the winner
    // we can use this to avoid duplicate action evaluation

    // DISABLE THIS

    return BrainScore(
        robotId,
        result.strategyResult,
        DistanceEstimate(0),
        strategy,
        result.finalState
    )
}

fun brainStep(
    initialGameState: GameState,
    currentState: GameState,
    strategy: Strategy,
    maximumSteps: Int
): Pair<GameState, Map<RobotId, Action>> {

    // one time step consists of advancing each worker wrapper by N moves
    // this will determine the order in which to advance robots by running
    // all robot states through all strategies, picking the the winning
    // robot/state pair and resuming until the stack of robots is empty

    // the list of robots can change over time steps, get a fresh copy each iteration
    var gameState = currentState
    val actions = mutableMapOf<RobotId, Action>()
    var workingSet = currentState.allRobotIds
    while (!gameState.isGameComplete() && workingSet.isNotEmpty()) {
        // pick the minimum across all robot/strategy pairs

        // work must be done in order
        val robotId = workingSet.first()!!

        val winner = strategySequence(robotId, strategy.compute(initialGameState), gameState)
            .take(if (workingSet.count() == 1) 1 else maximumSteps)
            .score(robotId, strategy)

        // we have a winner, remove it from the working set for this time step
        workingSet = workingSet.tailSet(robotId.nextId())

        // record the winning action and update the running state
        actions[winner.robotId] = winner.strategyResult.nextAction
        val nextGameState = winner.strategyResult.nextState
        gameState = nextGameState.copy(boardState = winner.simulationFinalState.boardState())
    }

    return actions.entries.fold(currentState) { state, entry ->
        applyAction(state, entry.key, entry.value)
    } to actions
}

data class BrainState(
    val gameState: GameState,
    val robotActions: Map<RobotId, List<Action>> = emptyMap(),
    val turn: Int = 0
)

fun brain(
    problem: Problem,
    strategy: Strategy,
    maximumSteps: Int
): Sequence<Solution> {
    val initalState = GameState(problem).initialize()
    return generateSequence(
        seed = BrainState(initalState),
        nextFunction = { brainState ->
            if (brainState.gameState.isGameComplete()) {
                null
            } else {
                val (newState, newActions) = brainStep(initalState, brainState.gameState, strategy, maximumSteps)
                val mergedActions = brainState.robotActions.toMutableMap()
                newActions.forEach { (robotId, action) ->
                    mergedActions.merge(robotId, listOf(action)) { left, right -> left.plus(right) }
                }

                BrainState(newState, mergedActions.toMap(), turn = brainState.turn + 1)
            }
        }
    ).drop(1).map { (_, actions) -> Solution(problem, actions) }
}
