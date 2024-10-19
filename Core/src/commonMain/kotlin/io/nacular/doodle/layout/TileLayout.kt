package io.nacular.doodle.layout

import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal

/**
 * A layout that tiles contents into squares that fit within the given area.
 *
 * @param orientation direction the tiling follows
 * @param spacing between tiles
 */
public class TileLayout(private val orientation: Orientation, private val spacing: Double): Layout {

    /**
     * Creates a [TileLayout] with [Horizontal] orientation.
     */
    public constructor(spacing: Double = 0.0): this(Horizontal, spacing)

    override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
        var numCols            = 1
        val children           = views.toList()
        var tileLimit          : Double
        var tileLength         = current.inlineExtent - insets.inlineTotal
        val availableTileSpace = current.orthogonalExtent - insets.orthogonalTrailing

        do {
            val numRows = (children.size / numCols) + 1
            tileLimit   = insets.orthogonalLeading + numRows * tileLength + (numRows - 1) * spacing

            if (tileLimit <= availableTileSpace) {
                break
            }

            ++numCols

            tileLength = (current.inlineExtent - insets.inlineTotal - (numCols - 1) * spacing) / numCols
        } while (true)

        val size = Size(tileLength)

        children.forEachIndexed { i, child ->
            when (orientation) {
                Horizontal -> child.updateBounds(
                    insets.left + i % numCols * (tileLength + spacing),
                    insets.top  + i / numCols * (tileLength + spacing),
                    size,
                    size
                )
                else -> child.updateBounds(
                    insets.left + i / numCols * (tileLength + spacing),
                    insets.top  + i % numCols * (tileLength + spacing),
                    size,
                    size
                )
            }
        }

        return current
    }

    private val Size.inlineExtent         get() = if (orientation == Horizontal) width        else height
    private val Size.orthogonalExtent     get() = if (orientation == Horizontal) height       else width

    private val Insets.orthogonalLeading  get() = if (orientation == Horizontal) top          else left
    private val Insets.orthogonalTrailing get() = if (orientation == Horizontal) bottom       else right
    private val Insets.inlineTotal        get() = if (orientation == Horizontal) left + right else top  + bottom
}