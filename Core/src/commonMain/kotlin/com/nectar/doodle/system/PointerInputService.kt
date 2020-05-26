package com.nectar.doodle.system


import com.nectar.doodle.geometry.Point


interface PointerInputService {
    var cursor         : Cursor
    var toolTipText    : String
    val pointerLocation: Point

    operator fun plusAssign (listener: Listener)
    operator fun minusAssign(listener: Listener)

    operator fun plusAssign (preprocessor: Preprocessor)
    operator fun minusAssign(preprocessor: Preprocessor)

    interface Listener {
        fun changed(event: SystemPointerEvent)
    }

    interface Preprocessor {
        fun preprocess(event: SystemPointerEvent) {}
    }
}
