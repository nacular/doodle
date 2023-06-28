package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import kotlin.math.max
import kotlin.math.min

private open class FlexLayout(private val orientation: Orientation, private val spacing: Double = 0.0): Layout {
    private var Positionable.offset get() = when (orientation) {
        Horizontal -> x
        else       -> y
    }; set(new) {
        when (orientation) {
            Horizontal -> x = new
            else       -> y = new
        }
    }

    private val Positionable.extent get() = when (orientation) {
        Horizontal -> width
        else       -> height
    }

    private val Positionable.minExtent get() = with(minimumSize) {
        when (orientation) {
            Horizontal -> width
            else       -> height
        }
    }

    private val Positionable.idealExtent get() = with(idealSize) {
        when (orientation) {
            Horizontal -> this?.width
            else       -> this?.height
        }
    }

    private val PositionableContainer.extent get() = when (orientation) {
        Horizontal -> width
        else       -> height
    }

    private val PositionableContainer.offset get() = when (orientation) {
        Horizontal -> insets.left
        else       -> insets.top
    }

    private val Insets.extent get() = when (orientation) {
        Horizontal -> left + right
        else       -> top  + bottom
    }

    override fun layout(container: PositionableContainer) {
        val x                      = container.insets.left
        val y                      = container.insets.top
        val w                      = max(0.0, container.width  - container.insets.run { left + right })
        val h                      = max(0.0, container.height - container.insets.run { top + bottom })
        var offset                 = container.offset
        val totalAvailableSpace    = container.extent - container.insets.extent
        var remainingSpace         = totalAvailableSpace
        val visibleChildren        = container.children.filter { it.visible }
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
            remainingSpace = totalAvailableSpace - offset + container.offset
        }

        visibleChildren.forEachIndexed { index, child ->
            val extent = child.idealExtent ?: child.minExtent

            if (index > 0) {
                when {
                    extent > 0.0 -> offset += spacing
                    else    -> ++numCollapsedSpaces
                }
            }

            child.bounds = when (orientation) {
                Horizontal -> Rectangle(offset, y, extent, h)
                else       -> Rectangle(x, offset, w, extent)
            }

            offset += child.extent

            updateRemainingSpace()
            updateMinDecreaseInfo(child)
        }

        while (remainingSpace != 0.0) {
            offset = container.offset

            when {
                remainingSpace > 0.0 -> {
                    val proportionalIncrease = max(0.0, remainingSpace - numCollapsedSpaces * spacing) / numChildrenCanIncrease
                    visibleChildren.forEachIndexed { index, child ->
                        val newExtent = child.extent + proportionalIncrease
                        if (index > 0 && newExtent > 0.0) {
                            offset += spacing
                        }

                        child.bounds = when (orientation) {
                            Horizontal -> Rectangle(offset,  child.y, newExtent,   child.height)
                            else       -> Rectangle(child.x, offset,  child.width, newExtent   )
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

                                        child.bounds = when (orientation) {
                                            Horizontal -> Rectangle(offset,  child.y, child.extent - proportionalDecrease, child.height)
                                            else       -> Rectangle(child.x, offset,  child.width, child.extent - proportionalDecrease)
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
    }

    override fun requiresLayout(
        child: Positionable,
        of  : PositionableContainer,
        old : View.SizePreferences,
        new : View.SizePreferences
    ): Boolean = old.idealSize != new.idealSize || old.minimumSize != new.minimumSize
}

/**
 * [Layout] that stacks children vertically with optional spacing. It scales Views
 * up to fit the parent's width and take up as much vertical room as possible. But
 * it respects [minimum height][View.minimumSize], and won't make Views shorter.
 *
 * @param spacing between Views
 */
public class FlexColumn(spacing: Double): Layout by FlexLayout(orientation = Vertical, spacing)

/**
 * [Layout] that stacks children horizontally with optional spacing. It scales Views
 * up to fit the parent's height and take up as much horizontal room as possible. But
 * it respects [minimum width][View.minimumSize], and won't make Views narrower.
 *
 * @param spacing between Views
 */
public class FlexRow(spacing: Double): Layout by FlexLayout(orientation = Horizontal, spacing)