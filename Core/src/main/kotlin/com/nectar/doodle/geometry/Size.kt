package com.nectar.doodle.geometry


class Size(val width: Double = 0.0, val height: Double = 0.0) {
    init {
        require(width  >= 0) { "Width cannot be negative"  }
        require(height >= 0) { "Height cannot be negative" }
    }

    val area  = width * height
    val empty = area == 0.0

    operator fun times(value: Double) = Size(width * value, height * value)
    operator fun div  (value: Double) = Size(width / value, height / value)

    override fun toString(): String = "[$width,$height]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size) return false

        if (width  != other.width ) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode() = arrayOf(width, height).contentHashCode()

    companion object {
        val Empty = Size(0.0, 0.0)
    }
}