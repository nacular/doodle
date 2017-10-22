package com.zinoti.jaz.event

import com.zinoti.jaz.core.Gizmo
import com.zinoti.jaz.geometry.Point


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