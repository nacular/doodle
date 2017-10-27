package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.SystemInputEvent.Modifier


class MouseWheelEvent(
        source: Gizmo,
        val location: Point,
        val xRotation: Int,
        val yRotation: Int,
        modifiers: Set<Modifier>) : InputEvent(source, modifiers)

/**
 * Informs listener that the mouse wheel has been rotated
 */
typealias MouseWheelListener = (MouseWheelEvent) -> Unit