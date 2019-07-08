package icfp2019.core

import icfp2019.model.Node
import icfp2019.model.Point
import org.pcollections.PVector
import org.pcollections.TreePVector

typealias GameMap<T> = PVector<PVector<T>>
typealias MapCells = GameMap<Node>

fun <T> MapCells.mapNodes(convert: (Node) -> T): GameMap<T> {
    return TreePVector.from(this.map {
        TreePVector.from(it.map { convert(it) })
    })
}

operator fun <T> List<List<T>>.get(point: Point): T =
    if (point !in this) throw IllegalArgumentException("Illegal Access $point")
    else this[point.x][point.y]

fun <T> PVector<PVector<T>>.update(point: Point, modifier: T.() -> T): PVector<PVector<T>> {
    return this.with(point.x, this[point.x].with(point.y, modifier(this[point])))
}

operator fun <N> List<List<N>>.contains(point: Point): Boolean =
    point.x in (0 until this.size) && point.y in (0 until this[point.x].size)
