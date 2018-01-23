package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.geometry.Point


abstract class AbstractLayout: Layout() {
    override fun child(of: Positionable, at: Point) = of.children.lastOrNull { it.visible && it.contains(at) }
}
