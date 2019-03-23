package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Insets

interface Container: Iterable<View> {
    var insets: Insets

    var layout: Layout?

    var isFocusCycleRoot: Boolean

    val children: MutableList<View>

    infix fun ancestorOf(view: View): Boolean

    fun child(at: Point): View?

    override fun iterator() = children.iterator()
}