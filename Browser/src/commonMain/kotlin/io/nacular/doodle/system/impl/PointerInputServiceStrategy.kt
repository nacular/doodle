package io.nacular.doodle.system.impl

import io.nacular.doodle.dom.MouseEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.SystemPointerEvent


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

internal interface PointerLocationResolver {
    operator fun invoke(event: MouseEvent): Point
}