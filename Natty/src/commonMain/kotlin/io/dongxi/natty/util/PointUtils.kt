package io.dongxi.natty.util

import io.nacular.doodle.geometry.Point
import kotlin.math.roundToInt

object PointUtils {

    fun textCenterXPoint(viewWidth: Double, textWidth: Double, y: Int): Point {
        return Point(textCenterX(viewWidth, textWidth), y)
    }


    private fun textCenterX(viewWidth: Double, textWidth: Double): Double {
        return (viewWidth - textWidth.roundToInt()) / 2
    }

}