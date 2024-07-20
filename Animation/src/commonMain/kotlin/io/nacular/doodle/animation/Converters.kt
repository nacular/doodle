package io.nacular.doodle.animation

import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Units
import io.nacular.measured.units.times
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Manages conversion between [T] and [V]. This is used by [NumericAnimationPlan] to support
 * arbitrary types that can be represented numerically.
 */
public sealed interface AnimationDataConverter<T, V> {
    /** The serialized representation of a zero [T] */
    public val zero: T

    /** Convert [value] to serialized form [V]. */
    public fun serialize(value: T): V

    /** Convert serialized [value] to deserialized form [T]. */
    public fun deserialize(value: V): T
}

/**
 * [AnimationDataConverter] that maps values [T] to [Double].
 */
public interface SingleDataConverter<T>: AnimationDataConverter<T, Double>

/**
 * [AnimationDataConverter] that maps values [T] to an [Array] of [Double].
 */
public interface MultiDataConverter<T> : AnimationDataConverter<T, Array<Double>> {
    /** The size of array this converter uses */
    public val size: Int
}

// region ================ Converters ========================

/**
 * Animation converter for [Int].
 */
public val Int.Companion.animationConverter: SingleDataConverter<Int> get() = object: SingleDataConverter<Int> {
    override val zero                       = 0
    override fun serialize  (value: Int   ) = value.toDouble()
    override fun deserialize(value: Double) = value.roundToInt()
}

/**
 * Animation converter for [Float].
 */
public val Float.Companion.animationConverter: SingleDataConverter<Float> get() = object: SingleDataConverter<Float> {
    override val zero                       = 0f
    override fun serialize  (value: Float ) = value.toDouble()
    override fun deserialize(value: Double) = value.toFloat()
}

/**
 * Animation converter for [Double].
 */
public val Double.Companion.animationConverter: SingleDataConverter<Double> get() = object: SingleDataConverter<Double> {
    override val zero                       = 0.0
    override fun serialize  (value: Double) = value
    override fun deserialize(value: Double) = value
}

/**
 * Animation converter for a [Measure].
 */
public val <T: Units> T.animationConverter: SingleDataConverter<Measure<T>> get() = object: SingleDataConverter<Measure<T>> {
    override val zero                           = 0     *    this@animationConverter
    override fun serialize  (value: Measure<T>) = value `in` this@animationConverter
    override fun deserialize(value: Double    ) = value *    this@animationConverter
}

/**
 * Animation converter for [Point] that allows `x` and `y` to animate.
 */
public val Point.Companion.animationConverter: MultiDataConverter<Point> get() = object: MultiDataConverter<Point> {
    override val size get()                        = 2
    override val zero                              = Origin
    override fun serialize  (value: Point        ) = arrayOf(value.x, value.y)
    override fun deserialize(value: Array<Double>) = Point(value[0], value[1])
}

/**
 * Animation converter for [Size] that allows `width` and `height` to animate.
 */
public val Size.Companion.animationConverter: MultiDataConverter<Size> get() = object: MultiDataConverter<Size> {
    override val size get()                        = 2
    override val zero                              = Empty
    override fun serialize  (value: Size         ) = arrayOf(value.width, value.height)
    override fun deserialize(value: Array<Double>) = Size(max(0.0, value[0]), max(0.0, value[1]))
}

/**
 * Animation converter for [Rectangle] that allows `x`, `y`, `width` and `height` to animate.
 */
public val Rectangle.Companion.animationConverter: MultiDataConverter<Rectangle> get() = object: MultiDataConverter<Rectangle> {
    override val size get()                        = 4
    override val zero                              = Empty
    override fun serialize  (value: Rectangle    ) = arrayOf(value.x, value.y, value.width, value.height)
    override fun deserialize(value: Array<Double>) = Rectangle(value[0], value[1], max(0.0, value[2]), max(0.0, value[3]))
}

/**
 * Animation converter for [Color] that allows its red, blue and green components to animate.
 */
public val Color.Companion.animationConverter: MultiDataConverter<Color> get() = object: MultiDataConverter<Color> {
    override val size get()                        = 4
    override val zero                              = Black opacity 0f
    override fun serialize  (value: Color        ) = arrayOf(value.red.toDouble(), value.green.toDouble(), value.blue.toDouble(), value.opacity.toDouble())
    override fun deserialize(value: Array<Double>) = Color(
        value[0].roundToInt().mod(256).toUByte(),
        value[1].roundToInt().mod(256).toUByte(),
        value[2].roundToInt().mod(256).toUByte(),
        max(0f, min(value[3].toFloat(), 1f))
    )
}

// endregion