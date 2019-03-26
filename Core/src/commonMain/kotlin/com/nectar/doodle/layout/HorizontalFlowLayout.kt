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
                           private val verticalSpacing  : Double              = 1.0,
                           private val horizontalSpacing: Double              = 1.0): Layout() {


    override fun layout(positionable: Positionable) {
        var y            = positionable.insets.top
        var height       = 0.0
        val itemList     = mutableListOf<View>()
        var lineWidth    = 0.0
        val maxLineWidth = positionable.width - positionable.insets.left - positionable.insets.right

        positionable.children.filter { it.visible }.forEach { child ->
            child.idealSize?.let { child.size = it }

            val temp = lineWidth + child.width + if (itemList.isNotEmpty()) horizontalSpacing else 0.0

            if (temp > maxLineWidth) {
                layoutLine(itemList, positionable, lineWidth, y)

                itemList.clear()

                lineWidth = 0.0

                if (height > 0) {
                    y += height + verticalSpacing
                }

                height = child.height
            }

            itemList.add(child)

            lineWidth = temp
            height    = max(height, child.height)
        }

        if (itemList.isNotEmpty()) {
            layoutLine(itemList, positionable, lineWidth, y)
        }
    }

    private fun layoutLine(itemList: List<View>, positionable: Positionable, lineWidth: Double, y: Double) {
        var startX = when (justification) {
            Right  ->  positionable.width - lineWidth - positionable.insets.right
            Center -> (positionable.width - lineWidth) / 2
            Left   ->  positionable.insets.left
        }

        itemList.forEach {
            it.position = Point(startX, y)

            startX += it.width + horizontalSpacing
        }
    }
}
