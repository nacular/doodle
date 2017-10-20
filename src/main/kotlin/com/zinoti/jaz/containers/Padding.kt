package com.zinoti.jaz.containers


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

class Padding private constructor(
        val top   : Double,
        val left  : Double,
        val bottom: Double,
        val right : Double) {

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
            val aPadding = other as Padding?

            return top == aPadding!!.top &&
                    left == aPadding.left &&
                    right == aPadding.right &&
                    bottom == aPadding.bottom
        }

        return false
    }

    companion object {
        /**
         * Creates a Padding that defines no offset from
         * the edges of the Container.
         *
         * This factory method provides a useful alternative
         * to a null Padding instance.
         *
         * @return
         */
        fun create(): Padding = NO_PADDING

        /**
         * Creates a Padding with equal offset from all edges.
         * This is equivalent to calling [.create]
         *
         * @param aValue The offset to use for top, left, bottom, and right
         * @return a Padding with equal offset from all edges
         */
        fun create(aValue: Double): Padding = Padding(aValue, aValue, aValue, aValue)

        /**
         * Creates a Padding with the specified offsets for each side.
         *
         * @param aTop    The top offset
         * @param aLeft   The left offset
         * @param aBottom The bottom offset
         * @param aRight  The right offset
         * @return a Padding with the respective offsets
         */
        fun create(aTop: Double, aLeft: Double, aBottom: Double, aRight: Double): Padding = Padding(aTop, aLeft, aBottom, aRight)

        val NO_PADDING = Padding(0.0, 0.0, 0.0, 0.0)
    }
}