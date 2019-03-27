package com.nectar.doodle.event

import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.SystemInputEvent.Modifier


class MouseScrollEvent(
        source: View,
        val location: Point,
        val xRotation: Double,
        val yRotation: Double,
        modifiers: Set<Modifier>): InputEvent(source, modifiers)

typealias MouseScrollListener = (source: View, event: MouseScrollEvent) -> Unit