package com.zinoti.jaz.drawing


class DashStyle private constructor(dash: Int, vararg rest: Int) {
    val dashes: IntArray = intArrayOf(dash) + rest

    companion object {
        fun create(dash: Int, vararg rest: Int) = DashStyle(dash, *rest)
    }
}

class Pen private constructor(val color: Color = Color.Black, val thickness: Double = 1.0, val dashStyle: DashStyle? = null) {

    val visible: Boolean = thickness > 0 && color.opacity > 0

    companion object {
        fun create() = Pen()

        fun create(color: Color) = Pen(color)

        fun create(color: Color, thickness: Double) = Pen(color, thickness)

        fun create(color: Color, thickness: Double, aDashStyle: DashStyle) = Pen(color, thickness, aDashStyle)
    }
}