package com.nectar.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
expect abstract class Performance {
    fun now(): Double
}

expect class Date {
    companion object {
        fun now(): Double
    }
}