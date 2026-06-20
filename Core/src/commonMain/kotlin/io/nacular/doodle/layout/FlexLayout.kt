package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import kotlin.math.max
import kotlin.math.min

private open class FlexLayout(private val orientation: Orientation, private val spacing: Double = 0.0): Layout {
    private var Positionable.offset get() = when (orientation) {
        Horizontal -> position.x
        else       -> position.y
    }; set(new) {
        when (orientation) {
            Horizontal -> { x = new; y = position.y }
            else       -> { x = position.x; y = new }
        }
    }

    private val Positionable.extent get() = when (orientation) {
        Horizontal -> width
        else       -> height
    }

    private val Positionable.idealExtent get() = when (orientation) {
        Horizontal -> idealSize.width
        else       -> idealSize.height
    }

    private val Positionable.minExtent get() = 0.0

    private val Size.extent get() = when (orientation) {
        Horizontal -> width
        else       -> height
    }

    private val Insets.extent get() = when (orientation) {
        Horizontal -> left + right
        else       -> top  + bottom
    }

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
        val x                      = insets.left
        val y                      = insets.top
        val w                      = max(0.0, current.width  - insets.run { left + right  })
        val h                      = max(0.0, current.height - insets.run { top  + bottom })
        var offset                 = if (orientation == Horizontal) x else y
        val totalAvailableSpace    = current.extent - insets.extent
        var remainingSpace         = totalAvailableSpace
        val visibleChildren        = views.filter { it.visible }.toList()
        var numCollapsedSpaces     = 0
        var minDecreaseAvailable   = -1.0
        var numChildrenCanDecrease = 0
        val numChildrenCanIncrease = visibleChildren.size
        val updateMinDecreaseInfo  = { child: Positionable ->
            if (child.extent > child.minExtent) {
                val decreaseAvailable = child.extent - child.minExtent
                minDecreaseAvailable = if (minDecreaseAvailable < 0.0) decreaseAvailable else min(
                    minDecreaseAvailable,
                    decreaseAvailable
                )
                ++numChildrenCanDecrease
            }
        }
        val updateRemainingSpace = {
            remainingSpace = totalAvailableSpace - (offset - if (orientation == Horizontal) x else y)
        }

        visibleChildren.forEachIndexed { index, child ->
            val extent = child.idealExtent

            if (index > 0) {
                when {
                    extent > 0.0 -> offset += spacing
                    else         -> ++numCollapsedSpaces
                }
            }

            when (orientation) {
                Horizontal -> {
                    child.x      = offset
                    child.y      = y
                    child.width  = extent
                    child.height = h
                }
                else       -> {
                    child.x      = x
                    child.y      = offset
                    child.width  = w
                    child.height = extent
                }
            }

            offset += child.extent

            updateRemainingSpace()
            updateMinDecreaseInfo(child)
        }

        while (remainingSpace != 0.0) {
            offset = if (orientation == Horizontal) x else y

            when {
                remainingSpace > 0.0 -> {
                    val proportionalIncrease = max(0.0, remainingSpace - numCollapsedSpaces * spacing) / numChildrenCanIncrease
                    visibleChildren.forEachIndexed { index, child ->
                        val newExtent = child.extent + proportionalIncrease
                        if (index > 0 && newExtent > 0.0) {
                            offset += spacing
                        }

                        when (orientation) {
                            Horizontal -> {
                                child.x      = offset
                                child.width  = newExtent
                            }
                            else       -> {
                                child.y      = offset
                                child.height = newExtent
                            }
                        }

                        offset += child.extent
                    }

                    remainingSpace = 0.0
                }

                remainingSpace < 0.0 -> {
                    when {
                        numChildrenCanDecrease > 0 -> {
                            val proportionalDecrease = min(minDecreaseAvailable, -remainingSpace / numChildrenCanDecrease)
                            minDecreaseAvailable     = -1.0
                            numChildrenCanDecrease   = 0

                            visibleChildren.forEachIndexed { index, child ->
                                when {
                                    child.extent > child.minExtent -> {
                                        val newExtent = child.extent - proportionalDecrease

                                        if (index > 0 && newExtent > 0.0) {
                                            offset += spacing
                                        }

                                        when (orientation) {
                                            Horizontal -> {
                                                child.x      = offset
                                                child.width  = child.extent - proportionalDecrease
                                            }
                                            else       -> {
                                                child.y      = offset
                                                child.height = child.extent - proportionalDecrease
                                            }
                                        }

                                        updateMinDecreaseInfo(child)
                                    }

                                    else -> {
                                        if (index > 0 && child.extent > 0.0) {
                                            offset += spacing
                                        }
                                        child.offset = offset
                                    }
                                }

                                offset += child.extent
                                updateRemainingSpace()
                            }
                        }
                        else -> break
                    }
                }
            }
        }

        return current
    }
}

/**
 * [Layout] that stacks children vertically with optional spacing. It scales Views
 * up to fit the parent's width and take up as much vertical room as possible.
 *
 * @param spacing between Views
 */
public class FlexColumn(spacing: Double): Layout by FlexLayout(orientation = Vertical, spacing)

/**
 * [Layout] that stacks children horizontally with optional spacing. It scales Views
 * up to fit the parent's height and take up as much horizontal room as possible.
 *
 * @param spacing between Views
 */
public class FlexRow(spacing: Double): Layout by FlexLayout(orientation = Horizontal, spacing)