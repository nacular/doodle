package com.nectar.doodle.layout

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import kotlin.math.max


class ListLayout constructor(private val spacing: Int = 0): Layout {

    override fun layout(gizmo: Gizmo) {
        // TODO: Can this be cleaned up to use idealSize?
        val insets = gizmo.insets_
        var y       = insets.top
        var width   = gizmo.run { idealSize?.width ?: width }

        gizmo.children_.asSequence().filter { it.visible }.forEach {
            width = max(width, it.idealSize?.width ?: it.width)
        }

        var i = 0

        gizmo.children_.asSequence().filter { it.visible }.forEach {
            it.bounds = Rectangle(insets.left, y, width, it.height)

            y += it.height + if (++i < gizmo.children_.size) spacing else 0
        }

        val size = Size(width + insets.left + insets.right, y + insets.bottom)

        gizmo.idealSize   = size // FIXME: Do we need this?
        gizmo.minimumSize = size

        if (gizmo.parent?.layout_ == null) {
            gizmo.size = size
        }

        gizmo.idealSize = size
    }

    override fun idealSize(gizmo: Gizmo, default: Size?): Size? {
        var width  = 0.0
        val insets = gizmo.insets_
        var y      = insets.top

        if (gizmo.children_.size > 0) {
            val firstChild = gizmo.children_.first()
            val idealSize  = firstChild.idealSize

            width = idealSize?.width ?: firstChild.width
        }

        var i = 0

        gizmo.children_.asSequence().filter { it.visible }.forEach {
            width = max(width, it.idealSize?.width ?: it.width)

            y += it.height + if (++i < gizmo.children_.size) spacing else 0
        }

        return Size(width + insets.left + insets.right, y + insets.bottom)
    }
}
