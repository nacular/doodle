package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
public expect abstract class Window {
    public fun requestAnimationFrame(callback: (Double) -> Unit): Int
    public fun cancelAnimationFrame(handle: Int)

    public abstract fun setTimeout   (handler: Any, timeout: Int, vararg arguments: Any?): Int
    public abstract fun clearTimeout (handle: Int)
    public abstract fun setInterval  (handler: Any, timeout: Int, vararg arguments: Any?): Int
    public abstract fun clearInterval(handle: Int)
}