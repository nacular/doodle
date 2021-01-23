package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
public actual abstract class Window {
    public actual abstract fun setTimeout(handler: Any, timeout: Int, vararg arguments: Any?): Int
    public actual abstract fun clearTimeout(handle: Int)
    public actual abstract fun setInterval(handler: Any, timeout: Int, vararg arguments: Any?): Int
    public actual abstract fun clearInterval(handle: Int)

    public actual fun requestAnimationFrame(callback: (Double) -> Unit): Int = 0

    public actual fun cancelAnimationFrame(handle: Int): Unit = Unit
}