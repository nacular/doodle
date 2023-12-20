package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
public expect abstract class Location {
    public open var host: String
}

public expect abstract class Window {
    public open val location: Location

    public fun matchMedia           (query: String): MediaQueryList
    public fun cancelAnimationFrame (handle: Int)
    public fun requestAnimationFrame(callback: (Double) -> Unit): Int
}

internal expect fun Window.setTimeout_   (handler: Any, timeout: Int, vararg arguments: Any?): Int
internal expect fun Window.clearTimeout_ (handle: Int)
internal expect fun Window.setInterval_  (handler: Any, timeout: Int, vararg arguments: Any?): Int
internal expect fun Window.clearInterval_(handle: Int)

internal expect val window: Window

public expect abstract class MediaQueryList {
    public val matches: Boolean
    public fun addListener(listener: ((Event) -> Unit)?)
}