package io.nacular.doodle.geometry

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
public class Size(public val width: Double = 0.0, public val height: Double = width) {

    public constructor(width: Int   = 0,  height: Int   = width): this(width.toDouble(), height.toDouble())
    public constructor(width: Float = 0f, height: Float = width): this(width.toDouble(), height.toDouble())

    init {
        require(width  >= 0) { "Width cannot be negative"  }
        require(height >= 0) { "Height cannot be negative" }
    }

    /** The area represented: [width] * [height] */
    public val area: Double by lazy { width * height }

    /** `true` IFF [area] == `0` */
    public val empty: Boolean by lazy { area == 0.0 }

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { arrayOf(width, height).contentHashCode() }

    override fun toString(): String = "[$width,$height]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size) return false

        return width == other.width && height == other.height
    }

    override fun hashCode(): Int = hashCode_

    internal fun fastEquals(other: Size): Boolean = this === other || (width == other.width && height == other.height)

    public companion object {
        /** The size with [width] and [height] equal to `0` */
        public val Empty: Size = Size(0.0)
    }
}

/**
 * Returns a [Size] with [width][Size.width] * value and [height][Size.height] * value
 *
 * @param value to multiply by
 */
public operator fun Size.times(value: Int   ): Size = Size(width * value, height * value)
public operator fun Size.times(value: Float ): Size = Size(width * value, height * value)
public operator fun Size.times(value: Double): Size = Size(width * value, height * value)

/**
 * Returns a [Size] with [width][Size.width] / value and [height][Size.height] / value
 *
 * @param value to divide by
 */
public operator fun Size.div(value: Int   ): Size = Size(width / value, height / value)
public operator fun Size.div(value: Double): Size = Size(width / value, height / value)
public operator fun Size.div(value: Float ): Size = Size(width / value, height / value)
