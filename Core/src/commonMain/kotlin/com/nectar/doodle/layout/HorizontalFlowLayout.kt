package com.nectar.doodle.layout

import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.HorizontalAlignment.Right
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 3/17/19.
 */
class HorizontalFlowLayout(private val justification    : HorizontalAlignment = Left,
                           private val verticalSpacing  : Double = 1.0,
                           private val horizontalSpacing: Double = 1.0): Layout() {


    override fun layout(positionable: Positionable) {
        var y            = positionable.insets.top
        var height       = 0.0
        val itemList     = mutableListOf<View>()
        var lineWidth    = 0.0
        val maxLineWidth = positionable.width - positionable.insets.left - positionable.insets.right

        positionable.children.forEach { child ->
            if (child.visible) {
                child.idealSize?.let { child.size = it }

                val temp = lineWidth + child.width + if (itemList.isNotEmpty()) horizontalSpacing else 0.0

                if (temp > maxLineWidth) {
                    when (justification) {
                        Right  -> layoutLine(itemList,  positionable.width - lineWidth - positionable.insets.right, y)
                        Center -> layoutLine(itemList, (positionable.width - lineWidth) / 2, y)
                        Left   -> layoutLine(itemList, positionable.insets.left, y)
                    }

                    itemList.clear()

                    lineWidth = 0.0

                    if (height > 0) {
                        y += height + verticalSpacing
                    }

                    height = child.height
                }

                lineWidth = temp

                itemList.add(child)

                height = max(height, child.height)
            }
        }

        if (itemList.isNotEmpty()) {
            when (justification) {
                Right  -> layoutLine(itemList,  positionable.width - lineWidth - positionable.insets.right, y)
                Center -> layoutLine(itemList, (positionable.width - lineWidth) / 2, y)
                Left   -> layoutLine(itemList, positionable.insets.left, y)
            }
        }
    }

    private fun layoutLine(itemList: List<View>, x: Double, y: Double) {
        var startX = x

        for (view in itemList) {
            view.position = Point(startX, y)

            startX += view.width + horizontalSpacing
        }
    }
}
