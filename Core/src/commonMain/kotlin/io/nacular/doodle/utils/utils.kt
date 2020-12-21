package io.nacular.doodle.utils

import kotlin.math.round

/**
 * Created by Nicholas Eddy on 10/20/17.
 */
val Int.isEven  get() = this % 2  == 0
val Long.isEven get() = this % 2L == 0L

inline val Int.isOdd  get() = !isEven
inline val Long.isOdd get() = !isEven

fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) {
        block()
    }

    return this
}

fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this) {
        block()
    }

    return this
}

/**
 * Ads [value] to the list at the specify index, or at the end of the
 * list if that index exceeds the current list length.
 *
 * @param at this index
 * @param value to insert or append
 */
fun <T> MutableList<T>.addOrAppend(at: Int, value: T) = when {
    at < size -> add(at, value)
    else      -> add(    value).run { Unit }
}

/**
 * Splits the character sequence based on a regex into a set of [MatchedChunk]s,
 * which indicates the match and the delimiter that separates it from its neighbor
 * on on the right.
 *
 * ```
 *
 * "a b c".splitMatches(\s) => ["a", " "], ["b", " "], remaining "c"
 *
 * ```
 *
 * The [limit] controls how the chunks are returned. A value of `0` will return all
 * matched values in the sequence. Any non-zero value will limit the number of
 * chunks. The rest of the string is given by [MatchResult.remaining].
 *
 * @param regex to split by
 * @param limit number of chunks to return
 */
fun CharSequence.splitMatches(regex: Regex, limit: Int = 0) = regex.splitMatches(this, limit)

class MatchResult(val matches: List<MatchedChunk>, val remaining: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchResult) return false

        if (matches != other.matches) return false
        if (remaining != other.remaining) return false

        return true
    }

    override fun hashCode(): Int {
        var result = matches.hashCode()
        result = 31 * result + remaining.hashCode()
        return result
    }
}

class MatchedChunk(val match: String, val delimiter: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchedChunk) return false

        if (match != other.match) return false
        if (delimiter != other.delimiter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = match.hashCode()
        result = 31 * result + delimiter.hashCode()
        return result
    }
}

/**
 * Splits the given character sequence based on this regex into a set of [MatchedChunk]s.
 *
 * @see CharSequence.splitMatches
 * @param input sequence
 * @param limit number of chunks to return
 */
fun Regex.splitMatches(input: CharSequence, limit: Int = 0): MatchResult {
    require(limit >= 0) { "Limit must be non-negative, but was $limit" }

    val matches = findAll(input).let { if (limit == 0) it else it.take(limit - 1) }
    val chunks  = mutableListOf<MatchedChunk>()
    var lastStart = 0

    for (match in matches) {
        chunks.add(MatchedChunk(input.subSequence(lastStart, match.range.first).toString(), match.value))
        lastStart = match.range.last + 1
    }

    return MatchResult(chunks, remaining = input.subSequence(lastStart, input.length).toString())
}

/**
 * Nullable helper
 * @see Collection.contains
 */
operator fun <E> Collection<E>?.contains(element: E): Boolean = this?.contains(element) == true

/**
 * Rounds this number to the nearest value.
 *
 * @param value to round to
 */
fun Double.roundToNearest(value: Double): Double = when (value) {
    0.0  -> this
    else -> round(this / value) * value
}