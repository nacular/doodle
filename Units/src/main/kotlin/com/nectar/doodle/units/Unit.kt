package com.nectar.doodle.units

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

open class Unit<T>(val display: String, val multiplier: Double = 1.0): Comparable<Unit<T>> {
    operator fun div(other: Unit<T>): Double = multiplier / other.multiplier

    operator fun <D> div(other: Unit<D>): Unit2<T, D> = Unit2(this, other)

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

open class Unit2<N, D>(private val numerator: Unit<N>, private val denominator: Unit<D>): Comparable<Unit2<N, D>> {
    private  val display    = " ${numerator.toString().trim()}/${denominator.toString().trim()}"
    internal val multiplier = numerator.multiplier / denominator.multiplier

    operator fun div(other: Unit2<N, D>): Double = multiplier / other.multiplier

    operator fun times(other: Unit<D>): Unit<N> = object : Unit<N>(numerator.display, numerator.multiplier * (other / denominator)) {}

    override fun compareTo(other: Unit2<N, D>): Int = multiplier.compareTo(other.multiplier)

    override fun toString() = display

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Unit2<*, *>) return false

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

    fun isZero() = magnitude == 0.0

    operator fun times(value: Int   ): Measure<T> = magnitude * value * unit
    operator fun times(value: Float ): Measure<T> = magnitude * value * unit
    operator fun times(value: Long  ): Measure<T> = magnitude * value * unit
    operator fun times(value: Double): Measure<T> = magnitude * value * unit

    operator fun div  (value: Int   ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Float ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Long  ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Double): Measure<T> = magnitude / value * unit

    operator fun <D> div(other: Measure<D>): Measure2<T, D> {
        return (unit / other.unit).let { (magnitude / other.magnitude) * it }
    }

    operator fun div(other: Measure<T>): Double = minOf(unit, other.unit).let {
        (this `in` it) / (other `in` it)
    }

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

open class Measure2<N, D>(internal val magnitude: Double, internal val unit: Unit2<N, D>): Comparable<Measure2<N, D>> {
    override fun compareTo(other: Measure2<N, D>) = minOf(unit, other.unit).let {
        ((this `in` it).compareTo((other `in` it)))
    }

    fun isZero() = magnitude == 0.0

    operator fun times(value: Int   ): Measure2<N, D> = magnitude * value * unit
    operator fun times(value: Float ): Measure2<N, D> = magnitude * value * unit
    operator fun times(value: Long  ): Measure2<N, D> = magnitude * value * unit
    operator fun times(value: Double): Measure2<N, D> = magnitude * value * unit

    operator fun times(other: Measure<D>) = (unit * other.unit).let { (magnitude * other.magnitude * it.multiplier) * it }

    operator fun div  (value: Int   ): Measure2<N, D> = magnitude / value * unit
    operator fun div  (value: Float ): Measure2<N, D> = magnitude / value * unit
    operator fun div  (value: Long  ): Measure2<N, D> = magnitude / value * unit
    operator fun div  (value: Double): Measure2<N, D> = magnitude / value * unit

    operator fun div(other: Measure2<N, D>): Double = minOf(unit, other.unit).let {
        (this `in` it) / (other `in` it)
    }

    operator fun unaryMinus(): Measure2<N, D> = Measure2(-magnitude, unit)

    operator fun plus(other: Measure2<N, D>): Measure2<N, D> {
        val resultUnit = minOf(unit, other.unit)

        return Measure2((this `in` resultUnit) + (other `in` resultUnit), resultUnit)
    }

    operator fun minus(other: Measure2<N, D>): Measure2<N, D> = this + -other

    infix fun `in`(other: Unit2<N, D>): Double = magnitude * (unit / other)

    infix fun `as`(other: Unit2<N, D>): Measure2<N, D> = if (unit == other) this else Measure2(`in`(other), other)

    override fun toString() = "$magnitude$unit"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Measure2<*, *>) return false

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

operator fun <T> Int   .times(unit: Unit<T>): Measure<T> = this.toDouble() * unit
operator fun <T> Float .times(unit: Unit<T>): Measure<T> = this.toDouble() * unit
operator fun <T> Long  .times(unit: Unit<T>): Measure<T> = this.toDouble() * unit
operator fun <T> Double.times(unit: Unit<T>): Measure<T> = Measure(this, unit)

operator fun <N, D> Int   .times(unit: Unit2<N, D>): Measure2<N, D> = this.toDouble() * unit
operator fun <N, D> Float .times(unit: Unit2<N, D>): Measure2<N, D> = this.toDouble() * unit
operator fun <N, D> Long  .times(unit: Unit2<N, D>): Measure2<N, D> = this.toDouble() * unit
operator fun <N, D> Double.times(unit: Unit2<N, D>): Measure2<N, D> = Measure2(this, unit)
