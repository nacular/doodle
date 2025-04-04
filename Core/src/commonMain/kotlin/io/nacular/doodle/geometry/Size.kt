package io.nacular.doodle.geometry

import io.nacular.doodle.utils.lerp
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.max

/**
 * A width and height pair that denote a rectangular area.
 *
 * @author Nicholas Eddy
 *
 * @property width Horizontal extent; not negative
 * @property height Vertical extent; not negative
 */
public class Size private constructor(public val width: Double = 0.0, public val height: Double = width) {

    /** The area represented: [width] * [height] */
    public val area: Double by lazy { width * height }

    /** `true` IFF [area] == `0` */
    public val empty: Boolean = width == 0.0 || height == 0.0

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
        /**
         * Creates a [Size].
         *
         * @param width Horizontal extent; cannot be negative
         * @param height Vertical extent; cannot be negative
         * @return a new Size
         */
        public operator fun invoke(width: Double = 0.0, height: Double = width): Size = Size(max(0.0, width), max(0.0, height))

        /**
         * Creates a [Size].
         *
         * @param width Horizontal extent; cannot be negative
         * @param height Vertical extent; cannot be negative
         * @return a new Size
         */
        public operator fun invoke(width: Int = 0, height: Int = width): Size = Size(width.toDouble(), height.toDouble())

        /**
         * Creates a [Size].
         *
         * @param width Horizontal extent; cannot be negative
         * @param height Vertical extent; cannot be negative
         * @return a new Size
         */
        public operator fun invoke(width: Float = 0f, height: Float = width): Size = Size(width.toDouble(), height.toDouble())

        /** The size with [width] and [height] equal to `0` */
        public val Empty: Size = Size(0.0)

        /** The size with [width] and [height] equal to [POSITIVE_INFINITY] */
        public val Infinite: Size = Size(POSITIVE_INFINITY)
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

/**
 * Interpolates between 2 [Size]s
 */
public fun lerp(first: Size, second: Size, fraction: Float): Size = Size(
    lerp(first.width,  second.width,  fraction),
    lerp(first.height, second.height, fraction)
)

public fun Size.coerceIn(min: Size, max: Size): Size = Size(width.coerceIn(min.width, max.width), height.coerceIn(min.height, max.height))