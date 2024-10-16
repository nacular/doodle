package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.WidthSource.Children
import io.nacular.doodle.layout.WidthSource.Parent
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.max


public enum class WidthSource {
    Parent, Children
}

public class ListLayout(
    private val spacing    : Double      = 0.0,
    private val widthSource: WidthSource = Children,
    private val minHeight  : Double      = 0.0,
    private val maxHeight  : Double      = POSITIVE_INFINITY
): Layout {

    public constructor(spacing: Int, widthSource: WidthSource = Children): this(spacing.toDouble(), widthSource)

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
        var maxWidth         = 0.0
        var y                = insets.top
        val horizontalInsets = insets.run { left + right  }

        views.filter { it.visible }.forEach {
            val widths = when (widthSource) {
                Parent -> current.width - horizontalInsets to current.width     - horizontalInsets
                else   -> 0.0                              to POSITIVE_INFINITY - horizontalInsets
            }

            it.updateBounds(insets.left, y, Size(widths.first, minHeight), Size(widths.second, maxHeight))

            y        += it.bounds.height + spacing
            maxWidth  = max(it.bounds.width, maxWidth)
        }

        return Size(maxWidth, y - spacing + insets.bottom)
    }
}
