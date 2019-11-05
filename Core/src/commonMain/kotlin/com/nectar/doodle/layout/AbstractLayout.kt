package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.geometry.Point


abstract class AbstractLayout: Layout() {
    override fun child(of: PositionableContainer, at: Point) = of.children.lastOrNull { it.visible && at in it }
}
