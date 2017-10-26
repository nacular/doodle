package com.nectar.doodle.geometry


class Size(val width: Double, val height: Double) {
    init {
        require(width  >= 0) { "Width cannot be negative"  }
        require(height >= 0) { "Height cannot be negative" }
    }

    val area  = width * height
    val empty = area == 0.0

    operator fun times(value: Number) = Size(width * value.toDouble(), height * value.toDouble())

    override fun toString(): String = "[$width,$height]"

    companion object {
        val Empty = Size(0.0, 0.0)
    }
}
/** Creates a (0,0) Dimension.  */