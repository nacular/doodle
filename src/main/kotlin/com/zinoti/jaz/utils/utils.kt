package com.zinoti.jaz.utils

/**
 * Created by Nicholas Eddy on 10/20/17.
 */
fun <T: Comparable<T>> min(first: T, second: T): T = if (first <= second) first else second

val Int.isEven get() = this % 2 == 0


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
