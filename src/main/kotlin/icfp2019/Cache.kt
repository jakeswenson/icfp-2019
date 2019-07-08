package icfp2019

import com.google.common.cache.CacheBuilder
import icfp2019.model.Board
import icfp2019.model.BoardStates
import icfp2019.model.GameState
import icfp2019.model.MovementSpeed

class Cache<T, Key, R> private constructor(private val filler: (T) -> R, private val keyGetter: (T) -> Key) {
    companion object {
        fun <R> forGameState(filler: (GameState) -> R): Cache<GameState, GameState, R> {
            return Cache(filler) { it }
        }

        fun <Key, R> forGameState(keyGetter: (GameState) -> Key, filler: (GameState) -> R): Cache<GameState, Key, R> {
            return Cache(filler, keyGetter)
        }

        fun <R> forBoard(filler: (Board) -> R): Cache<Board, Board, R> {
            return Cache(filler) { it }
        }

        fun <R> forBoardState(filler: (BoardStates) -> R): Cache<BoardStates, BoardStates, R> {
            return Cache(filler) { it }
        }

        fun <R> forBoardAndSpeed(filler: (Pair<Board, MovementSpeed>) -> R) =
            Cache(filler) { it }
    }

    private val storage = CacheBuilder.newBuilder()
        .maximumSize(50)
        .build<Key, R>()

    operator fun invoke(key: T): R {
        return storage.get(keyGetter(key)) {
            filler(key)
        }
    }
}
