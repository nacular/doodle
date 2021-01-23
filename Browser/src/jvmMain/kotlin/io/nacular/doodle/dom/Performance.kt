package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
public actual abstract class Performance {
    public actual fun now(): Double = 0.0
}

public actual class Date {
    public actual companion object {
        public actual fun now(): Double = 0.0
    }
}