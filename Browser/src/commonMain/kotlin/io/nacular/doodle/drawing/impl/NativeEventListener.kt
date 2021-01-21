package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.EventTarget


internal interface NativeEventListener {
    fun onClick      (target: EventTarget?) = true
    fun onFocusGained(target: EventTarget?) = true
    fun onFocusLost  (target: EventTarget?) = true
    fun onKeyUp      (target: EventTarget?) = true
    fun onKeyDown    (target: EventTarget?) = true
    fun onKeyPress   (target: EventTarget?) = true
    fun onScroll     (target: EventTarget?) = true
    fun onChange     (target: EventTarget?) = true
    fun onInput      (target: EventTarget?) = true
}