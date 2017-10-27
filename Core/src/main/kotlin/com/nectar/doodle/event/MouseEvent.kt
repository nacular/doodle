package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemMouseEvent.Button
import com.nectar.doodle.system.SystemMouseEvent.Type


class MouseEvent(
        source        : Gizmo,
        val type      : Type,
        val location  : Point,
        val buttons   : Set<Button>,
        val clickCount: Int,
        modifiers     : Set<Modifier>): InputEvent(source, modifiers) {

    constructor(
            source    : Gizmo,
            type      : Type,
            location  : Point,
            button    : Button,
            clickCount: Int,
            modifiers : Set<Modifier>): this(source, type, location, setOf(button), clickCount, modifiers)

    override fun toString() = "${this::class.simpleName} -> ${source::class.simpleName}: $type $location $buttons"
}
