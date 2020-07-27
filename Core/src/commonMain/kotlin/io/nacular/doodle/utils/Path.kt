package io.nacular.doodle.utils

import kotlin.math.min

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
class Path<T>(private val items: List<T>): Iterable<T> {
    constructor(item: T): this(listOf(item))
    constructor(       ): this(listOf()    )

    val top    = items.firstOrNull()
    val bottom = items.lastOrNull ()
    val depth  = items.size

    val parent by lazy { if (items.isNotEmpty()) Path(items.dropLast(1)) else null }

    private val hashCode = items.hashCode()

    operator fun get(index: Int) = items[index]

    infix fun ancestorOf(path: Path<T>): Boolean {
        if (depth >= path.depth) {
            return false
        }

        items.forEachIndexed { index, item ->
            if (item != path.items[index]) {
                return false
            }
        }

        return true
    }

    operator fun plus(node: T) = Path(items + node)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path<*>) return false

        // Kotlin list equals is not as efficient
        return items.size == other.items.size && items == other.items
    }

    fun overlappingRoot(other: Path<T>): Path<T> {
        val result = mutableListOf<T>()

        val depth = min(depth, other.depth)

        repeat(depth) {
            if (other.items[it] == items[it]) {
                result += items[it]
            }
        }

        return Path(result)
    }

    fun nonOverlappingStem(other: Path<T>): List<T> {
        val result = mutableListOf<T>()

        val smallestDepth = min(depth, other.depth)

        repeat(smallestDepth) {
            if (other.items[it] != items[it]) {
                result += items[it]
            }
        }

        (smallestDepth until depth).forEach {
            result += items[it]
        }

        return result
    }

    override fun hashCode() = hashCode
    override fun iterator() = items.iterator()
    override fun toString() = items.toString()
}
