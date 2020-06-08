package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
actual abstract class Window {
    actual abstract fun setTimeout(handler: Any, timeout: Int, vararg arguments: Any?): Int
    actual abstract fun clearTimeout(handle: Int)
    actual abstract fun setInterval(handler: Any, timeout: Int, vararg arguments: Any?): Int
    actual abstract fun clearInterval(handle: Int)

    actual fun requestAnimationFrame(callback: (Double) -> Unit) = 0

    actual fun cancelAnimationFrame(handle: Int) = Unit
}