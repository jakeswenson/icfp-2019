package icfp2019.core

import icfp2019.model.Node
import icfp2019.model.Point
import org.pcollections.PVector
import org.pcollections.TreePVector

typealias MapCells = PVector<PVector<Node>>

fun <T> MapCells.rebuild(convert: (Node) -> T): PVector<PVector<T>> {
    return TreePVector.from(this.map {
        TreePVector.from(it.map { convert(it) })
    })
}

operator fun <T> PVector<PVector<T>>.get(point: Point): T =
    if (point !in this) throw IllegalArgumentException("Illegal Access $point")
    else this[point.x][point.y]

fun <T> PVector<PVector<T>>.update(point: Point, modifier: T.() -> T): PVector<PVector<T>> {
    return this.with(point.x, this[point.x].with(point.y, modifier(this.get(point))))
}

operator fun <N> PVector<PVector<N>>.contains(point: Point): Boolean =
    point.x in (0 until this.size) && point.y in (0 until this[point.x].size)
