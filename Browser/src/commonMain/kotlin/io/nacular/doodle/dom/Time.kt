@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect abstract external class Performance {
    fun now(): Double
}

internal expect external class Date {
    companion object {
        fun now(): Double
    }
}