package io.nacular.doodle.dom

public actual abstract external class Performance {
    public actual fun now(): Double
}

public actual external class Date {
    public actual companion object {
        public actual fun now(): Double
    }
}