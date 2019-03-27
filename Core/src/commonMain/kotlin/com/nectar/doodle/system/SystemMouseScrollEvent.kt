package com.nectar.doodle.system

import com.nectar.doodle.geometry.Point

class SystemMouseScrollEvent(
            location : Point,
        val xRotation: Double,
        val yRotation: Double,
        modifiers: Set<Modifier>,
        nativeScrollPanel: Boolean = false): SystemPointerEvent(location, nativeScrollPanel, modifiers)