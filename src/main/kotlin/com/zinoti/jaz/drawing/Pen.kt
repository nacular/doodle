package com.zinoti.jaz.drawing


class DashStyle(dash: Int, vararg rest: Int) {
    val dashes: IntArray = intArrayOf(dash) + rest
}

class Pen(val color: Color = Color.black, val thickness: Double = 1.0, val dashStyle: DashStyle? = null) {
    val visible = thickness > 0 && color.opacity > 0
}