package com.nectar.doodle.layout

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.geometry.Point


abstract class AbstractLayout: Layout {
    override fun child(of: Gizmo, at: Point) = of.children_.lastOrNull { it.visible && it.contains(at) }
}
