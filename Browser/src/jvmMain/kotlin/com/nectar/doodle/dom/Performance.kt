package com.nectar.doodle.dom

/**
 * Created by Nicholas Eddy on 3/13/20.
 */
actual abstract class Performance {
    actual fun now() = 0.0
}

actual class Date {
    actual companion object {
        actual fun now() = 0.0
    }
}