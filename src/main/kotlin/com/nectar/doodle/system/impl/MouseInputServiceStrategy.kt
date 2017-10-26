package com.nectar.doodle.system.impl

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseWheelEvent


interface MouseInputServiceStrategy {
    var cursor       : Cursor
    var toolTipText  : String
    val mouseLocation: Point

    fun startUp (handler: EventHandler)
    fun shutdown()

    interface EventHandler {
        fun handle(event: SystemMouseEvent)
        fun handle(event: SystemMouseWheelEvent)
    }
}
