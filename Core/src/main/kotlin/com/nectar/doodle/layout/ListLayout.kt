package com.nectar.doodle.layout

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.WidthSource.Children
import com.nectar.doodle.layout.WidthSource.Parent
import kotlin.math.max


enum class WidthSource {
    Parent, Children
}

class ListLayout constructor(private val spacing: Int = 0, private val widthSource: WidthSource = Children): Layout {

    override fun layout(gizmo: Gizmo) {
        // TODO: Can this be cleaned up to use idealSize?
        val insets = gizmo.insets_
        var y      = insets.top

        val width = when (widthSource) {
            WidthSource.Parent -> gizmo.run { idealSize?.width ?: width }
            else               -> gizmo.children_.asSequence().filter { it.visible }.map{ it.idealSize?.width ?: it.width }.max() ?: 0.0
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
    }

    override fun idealSize(gizmo: Gizmo, default: Size?): Size? {
        val insets = gizmo.insets_
        var y      = insets.top

        var width = when (widthSource) {
            Parent -> gizmo.width
            else   -> gizmo.children_.firstOrNull()?.let { it.idealSize?.width ?: it.width } ?: 0.0
        }

        var i = 0

        gizmo.children_.asSequence().filter { it.visible }.forEach {
            if (widthSource == Children) {
                width = max(width, it.idealSize?.width ?: it.width)
            }

            y += it.height + if (++i < gizmo.children_.size) spacing else 0
        }

        return Size(width + insets.left + insets.right, y + insets.bottom)
    }
}
