package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.WidthSource.Children
import io.nacular.doodle.layout.WidthSource.Parent
import kotlin.Double.Companion.POSITIVE_INFINITY


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

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size): Size {
        // TODO: Can this be cleaned up to use idealSize?
        var y = 0.0

        val width = when (widthSource) {
            Parent -> current.width
            else   -> views.filter { it.visible }.maxOf { it.updateBounds(0.0, 0.0, Size(0.0, minHeight), Size(POSITIVE_INFINITY, maxHeight)).width }
        }.coerceIn(0.0, max.width)

        views.filter { it.visible }.forEach {
            it.updateBounds(it.bounds.x, y, Size(width, minHeight), Size(width, maxHeight))

            y += it.bounds.height + spacing
        }

        return Size(width, y - spacing)
    }
}
