package com.nectar.doodle.theme

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo

/**
 * Created by Nicholas Eddy on 2/13/18.
 */
class CompositeTheme(private vararg val themes: Theme): Theme {
    override fun install  (display: Display, all: Sequence<Gizmo>) = all.forEach { gizmo -> themes.forEach { it.install  (display, sequenceOf(gizmo)) } }
    override fun uninstall(display: Display, all: Sequence<Gizmo>) = all.forEach { gizmo -> themes.forEach { it.uninstall(display, sequenceOf(gizmo)) } }
}