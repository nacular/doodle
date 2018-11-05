package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Insets

interface Container: Iterable<View> {
    var insets: Insets

    var layout: Layout?

    var isFocusCycleRoot: Boolean

    val children: List<View>

    fun setZIndex(of: View, to: Int)
    fun zIndex(of: View): Int

    infix fun ancestorOf(view: View): Boolean

    fun child(at: Point): View?
}