package com.nectar.doodle.containers


/**
 * Represents the region within a Container's boundary that
 * should be used to bound it's contents.  Each of the 4 sides
 * are specified independently as follows:
 *
 * container
 * +-------------------+
 * |       t           |
 * |   +---------+     |
 * | l |         |  r  |
 * |   +---------+     |
 * |       b           |
 * +-------------------+
 *
 * The values l,t,r, and b represent the Left, Top, Right, and Bottom
 * values for the Padding respectively.
 *
 * @author Nicholas Eddy
 */

class Padding

    /**
     * Creates a Padding with the specified offsets for each side.
     *
     * @param top    The top offset
     * @param left   The left offset
     * @param bottom The bottom offset
     * @param right  The right offset
     * @return a Padding with the respective offsets
     */
    constructor(val top: Double, val left: Double, val bottom: Double, val right: Double) {

    /**
     * Creates a Padding that defines no offset from
     * the edges of the Container.
     *
     * This factory method provides a useful alternative
     * to a null Padding instance.
     *
     * @return
     */
    constructor(): this(0.0)


    /**
     * Creates a Padding with equal offset from all edges.
     * This is equivalent to calling [.create]
     *
     * @param value The offset to use for top, left, bottom, and right
     * @return a Padding with equal offset from all edges
     */
    constructor(value: Double): this(value, value, value, value)

    init {
        require(top    >= 0) { "Top cannot be negative"    }
        require(left   >= 0) { "Left cannot be negative"   }
        require(bottom >= 0) { "Bottom cannot be negative" }
        require(right  >= 0) { "Right cannot be negative"  }
    }

    override fun hashCode(): Int = arrayOf(top, left, right, bottom).contentHashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Padding) {
            return top    == other.top   &&
                   left   == other.left  &&
                   right  == other.right &&
                   bottom == other.bottom
        }

        return false
    }

    companion object {
        val None = Padding(0.0)
    }
}