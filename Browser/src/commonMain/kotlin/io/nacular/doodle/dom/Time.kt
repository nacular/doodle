package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
public expect abstract class Performance {
    public fun now(): Double
}

public expect class Date {
    public companion object {
        public fun now(): Double
    }
}