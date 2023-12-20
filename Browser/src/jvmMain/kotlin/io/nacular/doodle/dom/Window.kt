package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
public actual abstract class Location {
    public actual open var host: String = ""
}

public actual abstract class Window {
    public actual open val location: Location = object: Location() {}

    public actual fun matchMedia           (query: String): MediaQueryList = object: MediaQueryList() {}
    public actual fun cancelAnimationFrame (handle: Int): Unit = Unit
    public actual fun requestAnimationFrame(callback: (Double) -> Unit): Int = 0
}

internal actual fun Window.setTimeout_(handler: Any, timeout: Int, vararg arguments: Any?): Int = 0
internal actual fun Window.clearTimeout_(handle: Int) {}
internal actual fun Window.setInterval_(handler: Any, timeout: Int, vararg arguments: Any?): Int = 0
internal actual fun Window.clearInterval_(handle: Int) {}

internal actual val window = object: Window() {}

public actual abstract class MediaQueryList {
    public actual val matches: Boolean = false

    public actual fun addListener(listener: ((Event) -> Unit)?) {}
}