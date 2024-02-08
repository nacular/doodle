package io.nacular.doodle.dom

internal actual abstract class Performance {
    public actual fun now(): Double = 0.0
}

internal actual class Date {
    public actual companion object {
        public actual fun now(): Double = 0.0
    }
}