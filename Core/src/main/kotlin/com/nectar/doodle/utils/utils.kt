package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 10/20/17.
 */
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
