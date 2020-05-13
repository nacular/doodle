package com.nectar.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
expect abstract class Window {
    fun requestAnimationFrame(callback: (Double) -> Unit): Int
    fun cancelAnimationFrame(handle: Int)

    abstract fun setTimeout   (handler: Any, timeout: Int, vararg arguments: Any?): Int
    abstract fun clearTimeout (handle: Int)
    abstract fun setInterval  (handler: Any, timeout: Int, vararg arguments: Any?): Int
    abstract fun clearInterval(handle: Int)
}