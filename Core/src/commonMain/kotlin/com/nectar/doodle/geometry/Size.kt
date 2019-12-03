package com.nectar.doodle.geometry

/**
 * A width and height pair that denote a rectangular area.
 *
 * @author Nicholas Eddy
 *
 * @property width Horizontal extent; not negative
 * @property height Vertical extent; not negative
 *
 * @constructor
 * @param width Horizontal extent; cannot be negative
 * @param height Vertical extent; cannot be negative
 */
class Size(val width: Double = 0.0, val height: Double = width) {

    constructor(width: Int   = 0,  height: Int   = width): this(width.toDouble(), height.toDouble())
    constructor(width: Float = 0f, height: Float = width): this(width.toDouble(), height.toDouble())

    init {
        require(width  >= 0) { "Width cannot be negative"  }
        require(height >= 0) { "Height cannot be negative" }
    }

    /** The area represented: [width] * [height] */
    val area  = width * height

    /** `true` IFF [area] == `0` */
    val empty = area == 0.0

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { arrayOf(width, height).contentHashCode() }

    /**
     * Returns a Size with [width] * value and [height] * value
     *
     * @param value to multiply by
     */
    operator fun times(value: Int   ) = Size(width * value, height * value)
    operator fun times(value: Float ) = Size(width * value, height * value)
    operator fun times(value: Double) = Size(width * value, height * value)

    /**
     * Returns a Size with [width] / value and [height] / value
     *
     * @param value to divide by
     */
    operator fun div(value: Int   ) = Size(width / value, height / value)
    operator fun div(value: Float ) = Size(width / value, height / value)
    operator fun div(value: Double) = Size(width / value, height / value)

    override fun toString() = "[$width,$height]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size) return false

        return width == other.width && height == other.height
    }

    override fun hashCode() = hashCode_

    companion object {
        /** The size with [width] and [height] equal to `0` */
        val Empty = Size(0.0)
    }
}