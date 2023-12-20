package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
internal actual typealias Window   = org.w3c.dom.Window
internal actual typealias Location = org.w3c.dom.Location

internal actual inline fun Window.setTimeout_   (handler: Any, timeout: Int, vararg arguments: Any?) = this.setTimeout(handler, timeout, arguments)
internal actual inline fun Window.clearTimeout_ (handle: Int) = this.clearTimeout(handle)
internal actual inline fun Window.setInterval_  (handler: Any, timeout: Int, vararg arguments: Any?) = this.setInterval(handler, timeout, arguments)
internal actual inline fun Window.clearInterval_(handle: Int) = this.clearInterval(handle)

internal actual val window = kotlinx.browser.window

internal actual typealias MediaQueryList = org.w3c.dom.MediaQueryList