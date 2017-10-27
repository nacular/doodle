package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Rectangle


class BoundsEvent(source: Gizmo, val oldBounds: Rectangle, val newBounds: Rectangle): Event<Gizmo>(source) {

    val type: Set<Type>

    init {
        type = mutableSetOf()

        if (oldBounds.x != newBounds.x) {
            type.add(Type.X)
        }
        if (oldBounds.y != newBounds.y) {
            type.add(Type.Y)
        }
        if (oldBounds.width != newBounds.width) {
            type.add(Type.Width)
        }
        if (oldBounds.height != newBounds.height) {
            type.add(Type.Height)
        }
    }

    enum class Type {
        X, Y, Width, Height
    }
}

typealias BoundsListener = (BoundsEvent) -> Unit