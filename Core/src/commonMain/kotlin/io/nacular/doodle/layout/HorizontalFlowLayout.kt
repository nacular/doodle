package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.VerticalAlignment.Top
import kotlin.math.max

/**
 * [Layout] that positions children in order horizontally and wraps.
 */
public class HorizontalFlowLayout(private val justification    : HorizontalAlignment = Left,
                                  private val verticalSpacing  : Double              = 1.0,
                                  private val horizontalSpacing: Double              = 1.0,
                                  private val verticalAlignment: VerticalAlignment   = Top): Layout {

    public constructor(justification    : HorizontalAlignment = Left,
                       spacing          : Double              = 1.0,
                       verticalAlignment: VerticalAlignment   = Top): this(justification, spacing, spacing, verticalAlignment)

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
        var y            = insets.top
        var height       = 0.0
        val itemList     = mutableListOf<Positionable>()
        var lineWidth    = 0.0
        var lineHeight   = 0.0
        val maxLineWidth = current.width - insets.left - insets.right

        views.filter { it.visible }.forEach { child ->
            val childBounds = child.updateBounds(child.bounds.x, child.bounds.y, Size.Empty, Size.Infinite)

            lineHeight = max(lineHeight, childBounds.height)

            val temp = lineWidth + childBounds.width + if (itemList.isNotEmpty()) horizontalSpacing else 0.0

            if (temp > maxLineWidth) {
                layoutLine(itemList, current, insets, lineWidth, y, lineHeight)

                itemList.clear()

                lineWidth = child.bounds.width

                if (height > 0) {
                    y += height + verticalSpacing
                }

                height = child.bounds.height
            } else {
                lineWidth = temp
            }

            itemList.add(child)

            height = max(height, child.bounds.height)
        }

        if (itemList.isNotEmpty()) {
            layoutLine(itemList, current, insets, lineWidth, y, lineHeight)
        }

        return Size(current.width, y + lineHeight)
    }

    private fun layoutLine(itemList: List<Positionable>, parent: Size, insets: Insets, lineWidth: Double, lineY: Double, lineHeight: Double) {
        var startX = when (justification) {
            Right  ->  parent.width - lineWidth - insets.right
            Center -> (parent.width - lineWidth) / 2
            Left   ->  insets.left
        }

        itemList.forEach {
            val y = when (verticalAlignment) {
                Top    -> lineY
                Middle -> lineY + (lineHeight - it.bounds.height) / 2
                else   -> lineY +  lineHeight - it.bounds.height
            }

            it.bounds.at(startX, y).apply {
                it.position = Point(x, y)
            }

            startX += it.bounds.width + horizontalSpacing
        }
    }
}
