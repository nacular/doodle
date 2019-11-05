package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.WidthSource.Children
import com.nectar.doodle.layout.WidthSource.Parent
import kotlin.math.max


enum class WidthSource {
    Parent, Children
}

class ListLayout constructor(private val spacing: Int = 0, private val widthSource: WidthSource = Children): Layout() {

    override fun layout(container: PositionableContainer) {
        // TODO: Can this be cleaned up to use idealSize?
        val insets = container.insets
        var y      = insets.top

        val width = when (widthSource) {
            Parent -> container.run { idealSize?.width ?: width }
            else   -> container.children.asSequence().filter { it.visible }.map { it.idealSize?.width ?: it.width }.max() ?: 0.0
        }

        var i = 0

        container.children.asSequence().filter { it.visible }.forEach {
            it.bounds = Rectangle(insets.left, y, width, it.height)

            y += it.height + if (++i < container.children.size) spacing else 0
        }

        val size = Size(width + insets.left + insets.right, y + insets.bottom)

        container.idealSize   = size // FIXME: Do we need this?
        container.minimumSize = size

        if (container.parent?.layout == null) {
            container.size = size
        }
    }

    override fun idealSize(container: PositionableContainer, default: Size?): Size? {
        val insets = container.insets
        var y      = insets.top

        var width = when (widthSource) {
            Parent -> container.width
            else   -> container.children.firstOrNull()?.let { it.idealSize?.width ?: it.width } ?: 0.0
        }

        var i = 0

        container.children.asSequence().filter { it.visible }.forEach {
            if (widthSource == Children) {
                width = max(width, it.idealSize?.width ?: it.width)
            }

            y += it.height + if (++i < container.children.size) spacing else 0
        }

        return Size(width + insets.left + insets.right, y + insets.bottom)
    }
}
