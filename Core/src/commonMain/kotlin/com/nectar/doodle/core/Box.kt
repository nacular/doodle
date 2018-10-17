package com.nectar.doodle.core

import com.nectar.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 3/2/18.
 */
open class Box: Gizmo(), Container {
    init {
        focusable = false
    }

    override fun iterator() = children.iterator()

    override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    override var layout
        get(   ) = super.layout
        set(new) { super.layout = new }

    override var isFocusCycleRoot
        get(   ) = super.isFocusCycleRoot
        set(new) { super.isFocusCycleRoot = new }

    override val children = super.children

    override fun setZIndex(of: Gizmo, to: Int) = super.setZIndex(of, to)
    override fun zIndex(of: Gizmo) = super.zIndex(of)

    override fun ancestorOf(gizmo: Gizmo) = super.ancestorOf(gizmo)

    override fun child(at: Point) = super.child(at)
}