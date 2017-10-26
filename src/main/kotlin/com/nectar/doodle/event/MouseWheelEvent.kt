package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Point


class MouseWheelEvent(
        source: Gizmo,
        val position : Point,
        val xRotation: Int,
        val yRotation: Int,
        modifiers: Set<InputEvent.Modifier>) : InputEvent(source, modifiers)

/**
 * Informs listener that the mouse wheel has been rotated
 */
typealias MouseWheelListener = (MouseWheelEvent) -> Unit