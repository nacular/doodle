package io.nacular.doodle.utils

import kotlin.math.min

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
public class Path<T>(private val items: List<T>): Iterable<T> {
    public constructor(item: T): this(listOf(item))
    public constructor(       ): this(listOf()    )

    public val top   : T?  = items.firstOrNull()
    public val bottom: T?  = items.lastOrNull ()
    public val depth : Int = items.size

    public val parent: Path<T>? by lazy { if (items.isNotEmpty()) Path(items.dropLast(1)) else null }

    private val hashCode = items.hashCode()

    public operator fun get(index: Int): T = items[index]

    public infix fun ancestorOf(path: Path<T>): Boolean {
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

    public operator fun plus(node: T): Path<T> = Path(items + node)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path<*>) return false

        // Kotlin list equals is not as efficient
        return items.size == other.items.size && items == other.items
    }

    public fun overlappingRoot(other: Path<T>): Path<T> {
        val result = mutableListOf<T>()

        val depth = min(depth, other.depth)

        repeat(depth) {
            if (other.items[it] == items[it]) {
                result += items[it]
            }
        }

        return Path(result)
    }

    public fun nonOverlappingStem(other: Path<T>): List<T> {
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

    override fun hashCode(): Int         = hashCode
    override fun iterator(): Iterator<T> = items.iterator()
    override fun toString(): String      = items.toString()
}
