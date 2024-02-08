package io.nacular.doodle.dom

internal actual abstract external class Performance {
    actual fun now(): Double
}

internal actual external class Date {
    actual companion object {
        actual fun now(): Double
    }
}