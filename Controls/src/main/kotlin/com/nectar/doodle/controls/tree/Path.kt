package com.nectar.doodle.controls.tree

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
class Path<T>(private val items: List<T>): Iterable<T> {
    constructor(item: T): this(listOf(item))
    constructor(       ): this(listOf())

    val top   : T? get() = items.first()
    val bottom: T? get() = items.last ()
    val depth      get() = items.size

    val parent: Path<T>? get() = if (items.size > 1) {
        Path(items.drop(1))
    } else null

    operator fun get(index: Int) = items[index]

    fun isAncestor(path: Path<T>): Boolean {
        var parent: Path<T>? = path

        while (parent != null && depth <= parent.depth) {
            if (equals(parent)) {
                return true
            }

            parent = parent.parent
        }

        return false
    }

    fun getDecendant(node: T) = Path(items + node)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path<*>) return false

        if (items != other.items) return false

        return true
    }

    override fun iterator() = items.iterator()

    override fun hashCode() = items.hashCode()

    override fun toString(): String {
        val aValue = StringBuilder()

        for (aItem in items) {
            aValue.append("[" + aItem.toString() + "]")
        }

        return aValue.toString()
    }
}
