//package com.zinoti.jaz.containers
//
//import com.zinoti.jaz.core.Container
//import com.zinoti.jaz.core.Layout
//import com.zinoti.jaz.geometry.Point
//
//
//abstract class AbstractLayout: Layout {
//    override fun childAtPoint(container: Container, point: Point) = container.childrenByZIndex.firstOrNull { it.visible && it.containsPoint(point) }
//}
