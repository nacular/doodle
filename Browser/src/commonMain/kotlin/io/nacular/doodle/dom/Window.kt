@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect abstract external class Location {
    open var host: String
}

internal expect external class Navigator {
    val userAgent: String
}

internal expect abstract external class Window {
    open val location   : Location
    val navigator  : Navigator
    val performance: Performance?

    fun matchMedia           (query: String): MediaQueryList
    fun cancelAnimationFrame (handle: Int)
    fun requestAnimationFrame(callback: (Double) -> Unit): Int

    internal fun setTimeout   (handler: () -> Unit, timeout: Int, vararg arguments: JsAny?): Int
    internal fun clearTimeout (handle: Int)
    internal fun setInterval  (handler: () -> Unit, timeout: Int, vararg arguments: JsAny?): Int
    internal fun clearInterval(handle: Int)

    internal fun addEventListener   (eventName: String, callback: () -> Unit)
    internal fun removeEventListener(eventName: String, callback: () -> Unit)
}

internal expect val window: Window

internal expect abstract external class MediaQueryList {
    val matches: Boolean
    fun addListener(listener: ((Event) -> Unit)?)
}