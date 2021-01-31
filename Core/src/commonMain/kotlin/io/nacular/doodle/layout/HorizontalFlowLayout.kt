package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.VerticalAlignment
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.VerticalAlignment.Top
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 3/17/19.
 */
public class HorizontalFlowLayout(private val justification    : HorizontalAlignment = Left,
                                  private val verticalSpacing  : Double              = 1.0,
                                  private val horizontalSpacing: Double              = 1.0,
                                  private val verticalAlignment: VerticalAlignment   = Top): Layout {


    override fun layout(container: PositionableContainer) {
        var y            = container.insets.top
        var height       = 0.0
        val itemList     = mutableListOf<Positionable>()
        var lineWidth    = 0.0
        var lineHeight   = 0.0
        val maxLineWidth = container.width - container.insets.left - container.insets.right

        container.children.filter { it.visible }.forEach { child ->
            child.idealSize?.let { child.size = it }

            lineHeight = max(lineHeight, child.height)

            val temp = lineWidth + child.width + if (itemList.isNotEmpty()) horizontalSpacing else 0.0

            if (temp > maxLineWidth) {
                layoutLine(itemList, container, lineWidth, y, lineHeight)

                itemList.clear()

                lineWidth = child.width

                if (height > 0) {
                    y += height + verticalSpacing
                }

                height = child.height
            } else {
                lineWidth = temp
            }

            itemList.add(child)

            height = max(height, child.height)
        }

        if (itemList.isNotEmpty()) {
            layoutLine(itemList, container, lineWidth, y, lineHeight)
        }
    }

    private fun layoutLine(itemList: List<Positionable>, parent: PositionableContainer, lineWidth: Double, lineY: Double, lineHeight: Double) {
        var startX = when (justification) {
            Right  ->  parent.width - lineWidth - parent.insets.right
            Center -> (parent.width - lineWidth) / 2
            Left   ->  parent.insets.left
        }

        itemList.forEach {
            val y = when (verticalAlignment) {
                Top    -> lineY
                Middle -> lineY + (lineHeight - it.height) / 2
                else   -> lineY +  lineHeight - it.height
            }

            it.position = Point(startX, y)

            startX += it.width + horizontalSpacing
        }
    }
}
