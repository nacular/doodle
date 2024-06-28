package io.nacular.doodle.application

import io.nacular.doodle.dom.Window

internal fun isSafari(window: Window): Boolean {
    val userAgent = window.navigator.userAgent

    return "Safari" in userAgent && "Chrome" !in userAgent
}
