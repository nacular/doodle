package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.WidthSource.Children
import com.nectar.doodle.layout.WidthSource.Parent
import kotlin.math.max


enum class WidthSource {
    Parent, Children
}

class ListLayout constructor(private val spacing: Int = 0, private val widthSource: WidthSource = Children): Layout() {

    override fun layout(positionable: Positionable) {
        // TODO: Can this be cleaned up to use idealSize?
        val insets = positionable.insets
        var y      = insets.top

        val width = when (widthSource) {
            WidthSource.Parent -> positionable.run { idealSize?.width ?: width }
            else               -> positionable.children.asSequence().filter { it.visible }.map { it.idealSize?.width ?: it.width }.max() ?: 0.0
        }

        var i = 0

        positionable.children.asSequence().filter { it.visible }.forEach {
            it.bounds = Rectangle(insets.left, y, width, it.height)

            y += it.height + if (++i < positionable.children.size) spacing else 0
        }

        val size = Size(width + insets.left + insets.right, y + insets.bottom)

        positionable.idealSize   = size // FIXME: Do we need this?
        positionable.minimumSize = size

        if (positionable.parent?.layout_ == null) {
            positionable.size = size
        }
    }

    override fun idealSize(positionable: Positionable, default: Size?): Size? {
        val insets = positionable.insets
        var y      = insets.top

        var width = when (widthSource) {
            Parent -> positionable.width
            else   -> positionable.children.firstOrNull()?.let { it.idealSize?.width ?: it.width } ?: 0.0
        }

        var i = 0

        positionable.children.asSequence().filter { it.visible }.forEach {
            if (widthSource == Children) {
                width = max(width, it.idealSize?.width ?: it.width)
            }

            y += it.height + if (++i < positionable.children.size) spacing else 0
        }

        return Size(width + insets.left + insets.right, y + insets.bottom)
    }
}
