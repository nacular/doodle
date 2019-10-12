package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 10/20/17.
 */
val Int.isEven         get() = this % 2  == 0
val Long.isEven        get() = this % 2L == 0L

inline val Int.isOdd   get() = !isEven
inline val Long.isOdd  get() = !isEven

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

fun <T> MutableList<T>.addOrAppend(at: Int, value: T) = when {
    at < size -> add(at, value)
    else      -> add(    value).run { Unit }
}

fun CharSequence.splitMatches(regex: Regex, limit: Int = 0) = regex.splitMatches(this, limit)

fun Regex.splitMatches(input: CharSequence, limit: Int = 0): List<Pair<String, String>> {
    require(limit >= 0) { "Limit must be non-negative, but was $limit" }

    val matches = findAll(input).let { if (limit == 0) it else it.take(limit - 1) }
    val result = mutableListOf<Pair<String, String>>()
    var lastStart = 0

    for (match in matches) {
        result.add(input.subSequence(lastStart, match.range.start).toString() to match.value)
        lastStart = match.range.endInclusive + 1
    }

    result.add(input.subSequence(lastStart, input.length).toString() to "")
    return result
}

operator fun <E> Collection<E>?.contains(element: E): Boolean {
    return this?.contains(element) == true
}
