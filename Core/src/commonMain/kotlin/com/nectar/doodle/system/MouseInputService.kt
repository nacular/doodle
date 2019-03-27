package com.nectar.doodle.system


import com.nectar.doodle.geometry.Point


interface MouseInputService {

    var cursor       : Cursor
    var toolTipText  : String
    val mouseLocation: Point

    operator fun plusAssign (listener: Listener)
    operator fun minusAssign(listener: Listener)

    operator fun plusAssign (preprocessor: Preprocessor)
    operator fun minusAssign(preprocessor: Preprocessor)

    interface Listener {
        fun changed(event: SystemMouseEvent)
        fun changed(event: SystemMouseScrollEvent)
    }

    interface Preprocessor {
        fun preprocess(event: SystemMouseEvent     ) {}
        fun preprocess(event: SystemMouseScrollEvent) {}
    }
}
