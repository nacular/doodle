package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.Event


internal interface NativeEventListener {
    fun onClick      (event: Event) = true
    fun onFocusGained(event: Event) = true
    fun onFocusLost  (event: Event) = true
    fun onKeyUp      (event: Event) = true
    fun onKeyDown    (event: Event) = true
    fun onKeyPress   (event: Event) = true
    fun onScroll     (event: Event) = true
    fun onChange     (event: Event) = true
    fun onInput      (event: Event) = true
}