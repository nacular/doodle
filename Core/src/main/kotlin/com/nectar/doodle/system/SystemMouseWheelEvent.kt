package com.nectar.doodle.system

import com.nectar.doodle.geometry.Point

class SystemMouseWheelEvent(
        val location: Point,
        val xRotation: Int,
        val yRotation: Int,
        modifiers: Set<Modifier>): SystemInputEvent(modifiers)

/**
 * Informs listener that the mouse wheel has been rotated
 */
typealias SystemMouseWheelListener = (SystemMouseWheelEvent) -> Unit