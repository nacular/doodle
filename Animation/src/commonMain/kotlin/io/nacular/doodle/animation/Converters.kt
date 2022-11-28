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
 * [AnimationDataConverter] that maps values [V] to [Double].
 */
public interface SingleDataConverter<V>: AnimationDataConverter<V, Double>

/**
 * [AnimationDataConverter] that maps values [V] to an [Array] of [Double].
 */
public interface MultiDataConverter<V> : AnimationDataConverter<V, Array<Double>> {
    /** The size of array this converter uses */
    public val size: Int
}

// region ================ Converters ========================

public val Int.Companion.animationConverter: SingleDataConverter<Int> get() = object: SingleDataConverter<Int> {
    override val zero                       = 0
    override fun serialize  (value: Int   ) = value.toDouble()
    override fun deserialize(value: Double) = value.roundToInt()
}

public val Float.Companion.animationConverter: SingleDataConverter<Float> get() = object: SingleDataConverter<Float> {
    override val zero                       = 0f
    override fun serialize  (value: Float ) = value.toDouble()
    override fun deserialize(value: Double) = value.toFloat()
}

public val Double.Companion.animationConverter: SingleDataConverter<Double> get() = object: SingleDataConverter<Double> {
    override val zero                       = 0.0
    override fun serialize  (value: Double) = value
    override fun deserialize(value: Double) = value
}

public val <T: Units> T.animationConverter: SingleDataConverter<Measure<T>> get() = object: SingleDataConverter<Measure<T>> {
    override val zero                           = 0 * this@animationConverter
    override fun serialize  (value: Measure<T>) = value `in` this@animationConverter
    override fun deserialize(value: Double    ) = value * this@animationConverter
}

public val Point.Companion.animationConverter: MultiDataConverter<Point> get() = object: MultiDataConverter<Point> {
    override val size get()                        = 2
    override val zero                              = Origin
    override fun serialize  (value: Point        ) = arrayOf(value.x, value.y)
    override fun deserialize(value: Array<Double>) = Point(value[0], value[1])
}

public val Size.Companion.animationConverter: MultiDataConverter<Size> get() = object: MultiDataConverter<Size> {
    override val size get()                        = 2
    override val zero                              = Empty
    override fun serialize  (value: Size         ) = arrayOf(value.width, value.height)
    override fun deserialize(value: Array<Double>) = Size(max(0.0, value[0]), max(0.0, value[1]))
}

public val Rectangle.Companion.animationConverter: MultiDataConverter<Rectangle> get() = object: MultiDataConverter<Rectangle> {
    override val size get()                        = 4
    override val zero                              = Empty
    override fun serialize  (value: Rectangle    ) = arrayOf(value.x, value.y, value.width, value.height)
    override fun deserialize(value: Array<Double>) = Rectangle(value[0], value[1], max(0.0, value[2]), max(0.0, value[3]))
}

public val Color.Companion.animationConverter: MultiDataConverter<Color> get() = object: MultiDataConverter<Color> {
    override val size get()                        = 4
    override val zero                              = Black opacity 0f
    override fun serialize  (value: Color        ) = arrayOf(value.red.toDouble(), value.green.toDouble(), value.blue.toDouble(), value.opacity.toDouble())
    override fun deserialize(value: Array<Double>) = Color(value[0].roundToInt().toUByte(), value[1].roundToInt().toUByte(), value[2].roundToInt().toUByte(), max(0f, value[3].toFloat()))
}

// endregion