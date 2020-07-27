package io.nacular.doodle.layout

import io.nacular.doodle.geometry.Rectangle

/**
 * Represents offsets from a [Rectangle]'s boundary.  Each of the 4 sides are specified independently as follows:
 *
 * +-------------------+
 * |       t           |
 * |   +---------+     |
 * | l |         |  r  |
 * |   +---------+     |
 * |       b           |
 * +-------------------+
 *
 * The values l,t,r, and b represent the Left, Top, Right, and Bottom values for the Insets respectively.
 *
 * @author Nicholas Eddy
 */

class Insets
    /**
     * Creates a Insets with the specified value for each side.
     *
     * @param top    The top offset
     * @param left   The left offset
     * @param bottom The bottom offset
     * @param right  The right offset
     * @return a Insets with the respective values
     */
    constructor(val top: Double = 0.0, val left: Double = 0.0, val bottom: Double = 0.0, val right: Double = 0.0) {

    /**
     * Creates a Insets with equal offset from all edges.
     * This is equivalent to calling `Insets(all, all, all, all)`
     *
     * @param all The offset to use for top, left, bottom, and right
     * @return a Insets with equal offset from all edges
     */
    constructor(all: Double): this(all, all, all, all)

    override fun hashCode(): Int = arrayOf(top, left, right, bottom).contentHashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Insets) {
            return top    == other.top   &&
                   left   == other.left  &&
                   right  == other.right &&
                   bottom == other.bottom
        }

        return false
    }

    override fun toString() = "[$top, $left, $bottom, $right]"

    companion object {
        val None = Insets()
    }
}