package com.nectar.doodle.units

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

class Unit<T>(val display: String, val multiplier: Double = 1.0): Comparable<Unit<T>> {
    operator fun div(other: Unit<T>): Double = multiplier / other.multiplier

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

class Measure<T>(private val magnitude: Double, private val unit: Unit<T>): Comparable<Measure<T>> {
    override fun compareTo(other: Measure<T>): Int {
        return ((this `as` other.unit).magnitude - other.magnitude).toInt()
    }

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

operator fun <T> Int   .times(unit: Unit<T>): Measure<T> = this.toDouble() * unit
operator fun <T> Float .times(unit: Unit<T>): Measure<T> = this.toDouble() * unit
operator fun <T> Long  .times(unit: Unit<T>): Measure<T> = this.toDouble() * unit
operator fun <T> Double.times(unit: Unit<T>): Measure<T> = Measure(this, unit)