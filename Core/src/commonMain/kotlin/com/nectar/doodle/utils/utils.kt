package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 10/20/17.
 */
val Int.isEven  get() = this % 2  == 0
val Long.isEven get() = this % 2L == 0L

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