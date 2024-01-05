@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

public expect abstract external class Performance {
    public fun now(): Double
}

public expect external class Date {
    public companion object {
        public fun now(): Double
    }
}