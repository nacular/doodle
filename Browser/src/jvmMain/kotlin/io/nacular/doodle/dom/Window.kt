package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
internal actual abstract class Location {
    actual open var host: String = ""
}

internal actual class Navigator {
    actual val userAgent: String = ""
}

internal actual class VisualViewport {
    actual val offsetTop  = 0.0
    actual val offsetLeft = 0.0
}

internal actual abstract class Window {
    actual open val location: Location = object: Location() {}
    actual val performance: Performance? = null
    actual val navigator: Navigator = Navigator()
    actual val visualViewport: VisualViewport? = VisualViewport()

    actual fun matchMedia           (query: String): MediaQueryList = object: MediaQueryList() {}
    actual fun cancelAnimationFrame (handle: Int): Unit = Unit
    actual fun requestAnimationFrame(callback: (Double) -> Unit): Int = 0

    actual fun setTimeout   (handler: () -> Unit, timeout: Int, vararg arguments: JsAny?): Int = 0
    actual fun clearTimeout (handle: Int) {}
    actual fun setInterval  (handler: () -> Unit, timeout: Int, vararg arguments: JsAny?): Int = 0
    actual fun clearInterval(handle: Int) {}

    actual fun addEventListener(eventName: String, callback: () -> Unit) {}
    actual fun removeEventListener(eventName: String, callback: () -> Unit) {}
}

internal actual val window: Window = object: Window() {}

internal actual abstract class MediaQueryList {
    actual val matches: Boolean = false

    actual fun addListener(listener: ((Event) -> Unit)?) {}
}