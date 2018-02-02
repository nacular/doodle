package com.nectar.doodle.layout


/**
 * Represents the region within a Gizmo's boundary that
 * should be used to bound it's contents.  Each of the 4 sides
 * are specified independently as follows:
 *
 * Gizmo
 * +-------------------+
 * |       t           |
 * |   +---------+     |
 * | l |         |  r  |
 * |   +---------+     |
 * |       b           |
 * +-------------------+
 *
 * The values l,t,r, and b represent the Left, Top, Right, and Bottom
 * values for the Insets respectively.
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
     * This is equivalent to calling [.create]
     *
     * @param all The offset to use for top, left, bottom, and right
     * @return a Insets with equal offset from all edges
     */
    constructor(all: Double): this(all, all, all, all)

    init {
        require(top    >= 0) { "top cannot be negative"    }
        require(left   >= 0) { "left cannot be negative"   }
        require(bottom >= 0) { "bottom cannot be negative" }
        require(right  >= 0) { "right cannot be negative"  }
    }

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
        val None = Insets(0.0)
    }
}