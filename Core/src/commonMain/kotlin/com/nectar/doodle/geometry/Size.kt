package com.nectar.doodle.geometry


class Size(val width: Double = 0.0, val height: Double = width) {

    constructor(width: Int   = 0, height : Int   = width) : this(width.toDouble(), height.toDouble())
    constructor(width: Float = 0f, height: Float = width) : this(width.toDouble(), height.toDouble())

    init {
        require(width  >= 0) { "Width cannot be negative"  }
        require(height >= 0) { "Height cannot be negative" }
    }

    val area  = width * height
    val empty = area == 0.0

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { arrayOf(width, height).contentHashCode() }

    operator fun times(value: Double) = Size(width * value, height * value)
    operator fun div  (value: Double) = Size(width / value, height / value)

    override fun toString(): String = "[$width,$height]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size) return false

        return width == other.width && height == other.height
    }

    override fun hashCode() = hashCode_

    companion object {
        val Empty = Size(0.0)
    }
}