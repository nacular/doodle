package com.nectar.doodle.units

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

class Unit<T>(val display: String, val multiplier: Double = 1.0): Comparable<Unit<T>> {
    operator fun div(other: Unit<T>): Double = multiplier / other.multiplier

    override fun compareTo(other: Unit<T>): Int = multiplier.compareTo(other.multiplier)

    override fun toString() = display
}

class Measure<T>(private val magnitude: Double, private val unit: Unit<T>) {
    operator fun times(value: Int   ): Measure<T> = magnitude * value * unit
    operator fun times(value: Float ): Measure<T> = magnitude * value * unit
    operator fun times(value: Long  ): Measure<T> = magnitude * value * unit
    operator fun times(value: Double): Measure<T> = magnitude * value * unit

    operator fun div  (value: Int   ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Float ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Long  ): Measure<T> = magnitude / value * unit
    operator fun div  (value: Double): Measure<T> = magnitude / value * unit

    operator fun unaryMinus(): Measure<T> = Measure(-magnitude, unit)

    operator fun plus(other: Measure<T>): Measure<T> {
        val resultUnit = minOf(unit, other.unit)

        return Measure((this `in` resultUnit) + (other `in` resultUnit), resultUnit)
    }

    operator fun minus(other: Measure<T>): Measure<T> = this + -other

    infix fun `in`(other: Unit<T>): Double = magnitude * (other / unit)

    infix fun `as`(other: Unit<T>): Measure<T> = if (unit == other) this else Measure(`in`(other), other)

    override fun toString() = "$magnitude$unit"
}

operator fun <T> Int   .times(unit: Unit<T>): Measure<T> = Measure(this.toDouble(), unit)
operator fun <T> Float .times(unit: Unit<T>): Measure<T> = Measure(this.toDouble(), unit)
operator fun <T> Long  .times(unit: Unit<T>): Measure<T> = Measure(this.toDouble(), unit)
operator fun <T> Double.times(unit: Unit<T>): Measure<T> = Measure(this,            unit)