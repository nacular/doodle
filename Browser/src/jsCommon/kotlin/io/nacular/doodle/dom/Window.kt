package io.nacular.doodle.dom

internal actual abstract external class Location {
    actual open var host: String
}

internal actual external class Navigator {
    actual val userAgent: String
}

internal actual external class VisualViewport {
    actual val offsetTop : Double
    actual val offsetLeft: Double
}

internal actual abstract external class Window {
    actual val navigator     : Navigator
    actual val performance   : Performance?
    actual open val location: Location
    actual val visualViewport: VisualViewport?

    actual fun matchMedia           (query: String): MediaQueryList
    actual fun cancelAnimationFrame (handle: Int)
    actual fun requestAnimationFrame(callback: (Double) -> Unit): Int

    actual fun setTimeout   (handler: () -> Unit, timeout: Int, vararg arguments: JsAny?): Int
    actual fun clearTimeout (handle: Int)
    actual fun setInterval  (handler: () -> Unit, timeout: Int, vararg arguments: JsAny?): Int
    actual fun clearInterval(handle: Int)

    actual fun addEventListener   (eventName: String, callback: () -> Unit)
    actual fun removeEventListener(eventName: String, callback: () -> Unit)
}

internal actual external val window: Window

internal actual abstract external class MediaQueryList {
    actual val matches: Boolean
    actual fun addListener(listener: ((Event) -> Unit)?)
}
