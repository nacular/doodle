package com.nectar.doodle.units

import com.nectar.doodle.JvmName

/**
 * Created by Nicholas Eddy on 10/19/17.
 */


open class Unit<T>(val display: String, internal val multiplier: Double = 1.0): Comparable<Unit<T>> {
    operator fun     div(other: Unit<T>): Double          = multiplier / other.multiplier
    operator fun <D> div(other: Unit<D>): UnitRatio<T, D> = UnitRatio(this, other)

    operator fun     times(other: Unit<T>        ): UnitProduct<T, T> = UnitProduct(this, other)
    operator fun <N> times(other: UnitRatio<N, T>): Measure<N>        = other * this

    override fun compareTo(other: Unit<T>): Int = multiplier.compareTo(other.multiplier)

    override fun toString() = display

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Unit<*>) return false

        if (display    != other.display   ) return false
        if (multiplier != other.multiplier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = display.hashCode()
        result = 31 * result + multiplier.hashCode()
        return result
    }
}

open class UnitProduct<A, B>(private val first: Unit<A>, private val second: Unit<B>): Comparable<UnitProduct<A, B>> {
    private  val display    = if (first == second) " ${first.toString().trim()}²" else " ${first.toString().trim()}*${second.toString().trim()}"
    internal val multiplier = first.multiplier * second.multiplier

    operator fun div(other: UnitProduct<A, B>): Double = multiplier / other.multiplier

    @JvmName("divNumerator")
    operator fun div(other: Unit<A>): Measure<B> = first.multiplier / other.multiplier * second

    operator fun div(other: Unit<B>): Measure<A> = second.multiplier / other.multiplier * first

    override fun compareTo(other: UnitProduct<A, B>): Int = multiplier.compareTo(other.multiplier)

    override fun toString() = display

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnitProduct<*, *>) return false

        if (display    != other.display   ) return false
        if (multiplier != other.multiplier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = display.hashCode()
        result = 31 * result + multiplier.hashCode()
        return result
    }
}

open class UnitRatio<N, D>(private val numerator: Unit<N>, private val denominator: Unit<D>): Comparable<UnitRatio<N, D>> {
    private  val display    = " ${numerator.toString().trim()}/${denominator.toString().trim()}"
    internal val multiplier = numerator.multiplier / denominator.multiplier

    internal val reciprocal: UnitRatio<D, N> by lazy { UnitRatio(denominator, numerator) }

    operator fun div(other: UnitRatio<N, D>): Double = multiplier / other.multiplier

    operator fun times(other: Unit<D>): Measure<N> = other.multiplier / denominator.multiplier * numerator

    override fun compareTo(other: UnitRatio<N, D>): Int = multiplier.compareTo(other.multiplier)

    override fun toString() = display

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnitRatio<*, *>) return false

        if (display    != other.display   ) return false
        if (multiplier != other.multiplier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = display.hashCode()
        result = 31 * result + multiplier.hashCode()
        return result
    }
}

open class Measure<T>(internal val magnitude: Double, internal val unit: Unit<T>): Comparable<Measure<T>> {
    override fun compareTo(other: Measure<T>) = minOf(unit, other.unit).let {
        ((this `in` it).compareTo((other `in` it)))
    }

    val isZero get() = magnitude == 0.0

    operator fun times(value: Int   ): Measure<T> = magnitude * value * unit
    operator fun times(value: Float ): Measure<T> = magnitude * value * unit
    operator fun times(value: Long  ): Measure<T> = magnitude * value * unit
    operator fun times(value: Double): Measure<T> = magnitude * value * unit

    operator fun times(other: Measure<T>): MeasureProduct<T, T> = (magnitude * other.magnitude) * (unit * unit)

    operator fun div  (value: Int   ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Float ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Long  ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Double): Measure<T> = magnitude / value * unit

    operator fun div(other: Measure<T>): Double = minOf(unit, other.unit).let {
        (this `in` it) / (other `in` it)
    }

    operator fun <D> div(other: Measure<D>        ): MeasureRatio<T, D> = (unit / other.unit).let { (magnitude / other.magnitude) * it }
    operator fun <D> div(other: MeasureRatio<T, D>): Measure<D>         = (unit * other.unit.reciprocal).let { (magnitude / other.magnitude) * it }

    operator fun unaryMinus(): Measure<T> = Measure(-magnitude, unit)

    operator fun plus(other: Measure<T>): Measure<T> {
        val resultUnit = minOf(unit, other.unit)

        return Measure((this `in` resultUnit) + (other `in` resultUnit), resultUnit)
    }

    operator fun minus(other: Measure<T>): Measure<T> = this + -other

    infix fun `in`(other: Unit<T>): Double = magnitude * (unit / other)

    infix fun `as`(other: Unit<T>): Measure<T> = if (unit == other) this else Measure(`in`(other), other)

    override fun toString() = "$magnitude$unit"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Measure<*>) return false

        if (magnitude != other.magnitude) return false
        if (unit      != other.unit     ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = magnitude.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }
}

open class MeasureProduct<A, B>(internal val magnitude: Double, internal val unit: UnitProduct<A, B>): Comparable<MeasureProduct<A, B>> {
    override fun compareTo(other: MeasureProduct<A, B>) = minOf(unit, other.unit).let {
        ((this `in` it).compareTo((other `in` it)))
    }

    val isZero get() = magnitude == 0.0

    operator fun times(value: Int   ): MeasureProduct<A, B> = magnitude * value * unit
    operator fun times(value: Float ): MeasureProduct<A, B> = magnitude * value * unit
    operator fun times(value: Long  ): MeasureProduct<A, B> = magnitude * value * unit
    operator fun times(value: Double): MeasureProduct<A, B> = magnitude * value * unit

    operator fun div  (value: Int   ): MeasureProduct<A, B> = magnitude / value * unit
    operator fun div  (value: Float ): MeasureProduct<A, B> = magnitude / value * unit
    operator fun div  (value: Long  ): MeasureProduct<A, B> = magnitude / value * unit
    operator fun div  (value: Double): MeasureProduct<A, B> = magnitude / value * unit

    operator fun div(other: MeasureProduct<A, B>): Double = minOf(unit, other.unit).let {
        (this `in` it) / (other `in` it)
    }

    @JvmName("divSecond")
    operator fun div(other: Measure<B>): Measure<A> = magnitude / other.magnitude * (unit / other.unit)

    operator fun div(other: Measure<A>): Measure<B> = magnitude / other.magnitude * (unit / other.unit)

//    operator fun <D> div(other: Measure2<T, D>): Measure<D> {
//        return (unit * other.unit.reciprocal).let { (magnitude / other.magnitude) * it }
//    }

    operator fun unaryMinus(): MeasureProduct<A, B> = MeasureProduct(-magnitude, unit)

    operator fun plus(other: MeasureProduct<A, B>): MeasureProduct<A, B> {
        val resultUnit = minOf(unit, other.unit)

        return MeasureProduct((this `in` resultUnit) + (other `in` resultUnit), resultUnit)
    }

    operator fun minus(other: MeasureProduct<A, B>): MeasureProduct<A, B> = this + -other

    infix fun `in`(other: UnitProduct<A, B>): Double = magnitude * (unit / other)

    infix fun `as`(other: UnitProduct<A, B>): MeasureProduct<A, B> = if (unit == other) this else MeasureProduct(`in`(other), other)

    override fun toString() = "$magnitude$unit"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeasureProduct<*, *>) return false

        if (magnitude != other.magnitude) return false
        if (unit      != other.unit     ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = magnitude.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }
}

open class MeasureRatio<N, D>(internal val magnitude: Double, internal val unit: UnitRatio<N, D>): Comparable<MeasureRatio<N, D>> {
    override fun compareTo(other: MeasureRatio<N, D>) = minOf(unit, other.unit).let {
        ((this `in` it).compareTo((other `in` it)))
    }

    val isZero get() = magnitude == 0.0

    operator fun times(value: Int   ): MeasureRatio<N, D> = magnitude * value * unit
    operator fun times(value: Float ): MeasureRatio<N, D> = magnitude * value * unit
    operator fun times(value: Long  ): MeasureRatio<N, D> = magnitude * value * unit
    operator fun times(value: Double): MeasureRatio<N, D> = magnitude * value * unit

    operator fun times(other: Measure<D>): Measure<N> = magnitude * other.magnitude * (unit * other.unit)

    operator fun div  (value: Int   ): MeasureRatio<N, D> = magnitude / value * unit
    operator fun div  (value: Float ): MeasureRatio<N, D> = magnitude / value * unit
    operator fun div  (value: Long  ): MeasureRatio<N, D> = magnitude / value * unit
    operator fun div  (value: Double): MeasureRatio<N, D> = magnitude / value * unit

    operator fun div(other: MeasureRatio<N, D>): Double = minOf(unit, other.unit).let {
        (this `in` it) / (other `in` it)
    }

    operator fun unaryMinus(): MeasureRatio<N, D> = MeasureRatio(-magnitude, unit)

    operator fun plus(other: MeasureRatio<N, D>): MeasureRatio<N, D> {
        val resultUnit = minOf(unit, other.unit)

        return MeasureRatio((this `in` resultUnit) + (other `in` resultUnit), resultUnit)
    }

    operator fun minus(other: MeasureRatio<N, D>): MeasureRatio<N, D> = this + -other

    infix fun `in`(other: UnitRatio<N, D>): Double = magnitude * (unit / other)

    infix fun `as`(other: UnitRatio<N, D>): MeasureRatio<N, D> = if (unit == other) this else MeasureRatio(`in`(other), other)

    override fun toString() = "$magnitude$unit"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeasureRatio<*, *>) return false

        if (magnitude != other.magnitude) return false
        if (unit      != other.unit     ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = magnitude.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }
}


operator fun <T>    Number.times(value: Unit<T>          ): Measure<T>           = Measure       (this.toDouble(), value)
operator fun <N, D> Number.times(value: UnitRatio<N, D>  ): MeasureRatio<N, D>   = MeasureRatio  (this.toDouble(), value)
operator fun <A, B> Number.times(value: UnitProduct<A, B>): MeasureProduct<A, B> = MeasureProduct(this.toDouble(), value)

operator fun <T>    Number.times(value: Measure<T>          ): Measure<T>           = value * this.toDouble()
operator fun <N, D> Number.times(value: MeasureRatio<N, D>  ): MeasureRatio<N, D>   = value * this.toDouble()
operator fun <A, B> Number.times(value: MeasureProduct<A, B>): MeasureProduct<A, B> = value * this.toDouble()

fun <T>    abs(value: Measure<T>          ) = kotlin.math.abs(value.magnitude) * value.unit
fun <N, D> abs(value: MeasureRatio<N, D>  ) = kotlin.math.abs(value.magnitude) * value.unit
fun <A, B> abs(value: MeasureProduct<A, B>) = kotlin.math.abs(value.magnitude) * value.unit
