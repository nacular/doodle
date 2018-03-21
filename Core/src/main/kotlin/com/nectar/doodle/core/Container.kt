package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Insets

interface Container: Iterable<Gizmo> {
    var insets: Insets

    var layout: Layout?

    var isFocusCycleRoot: Boolean

    val children: List<Gizmo>

    fun setZIndex(of: Gizmo, to: Int)
    fun zIndex(of: Gizmo): Int

    fun isAncestor(of: Gizmo): Boolean

    fun child(at: Point): Gizmo?
}