package com.zinoti.jaz.units

import com.zinoti.jaz.utils.min

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

class Unit<T>(val display: String, val multiplier: Double = 1.0, base: Unit<T>? = null): Comparable<Unit<T>> {
    val base: Unit<T> = base ?: this

    operator fun div(other: Unit<T>): Double = multiplier / other.multiplier

    override fun compareTo(other: Unit<T>): Int = multiplier.compareTo(other.multiplier)

    override fun toString() = display
}

class Measure<T>(private val magnitude: Double, private val unit: Unit<T>) {
    operator fun times(value: Int   ) = magnitude * value
    operator fun times(value: Float ) = magnitude * value
    operator fun times(value: Long  ) = magnitude * value
    operator fun times(value: Double) = magnitude * value

    operator fun div  (value: Int   ) = magnitude / value
    operator fun div  (value: Float ) = magnitude / value
    operator fun div  (value: Long  ) = magnitude / value
    operator fun div  (value: Double) = magnitude / value

    operator fun plus(other: Measure<T>): Measure<T> {
        val resultUnit = min(unit, other.unit)

        return Measure((this `in` resultUnit) + (other `in` resultUnit), resultUnit)
    }

    infix fun `in`(other: Unit<T>): Double = magnitude * (unit / other)

    infix fun `as`(other: Unit<T>): Measure<T> = if (unit == other) this else Measure(`in`(other), other)

    override fun toString(): String = "$magnitude $unit"
}

operator fun <T> Int   .times(unit: Unit<T>): Measure<T> = Measure<T>(this.toDouble(), unit)
operator fun <T> Float .times(unit: Unit<T>): Measure<T> = Measure<T>(this.toDouble(), unit)
operator fun <T> Long  .times(unit: Unit<T>): Measure<T> = Measure<T>(this.toDouble(), unit)
operator fun <T> Double.times(unit: Unit<T>): Measure<T> = Measure<T>(this,            unit)