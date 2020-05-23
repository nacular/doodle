package com.nectar.doodle.system.impl

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemPointerEvent


internal interface PointerInputServiceStrategy {
    var cursor         : Cursor
    var toolTipText    : String
    val pointerLocation: Point

    fun startUp (handler: EventHandler)
    fun shutdown()

    interface EventHandler {
        fun handle(event: SystemPointerEvent)
    }
}
