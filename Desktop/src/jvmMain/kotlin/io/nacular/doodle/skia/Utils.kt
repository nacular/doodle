package io.nacular.doodle.skia

import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Rectangle
import org.jetbrains.skija.PathEffect
import org.jetbrains.skija.RRect
import org.jetbrains.skija.Rect

/**
 * Created by Nicholas Eddy on 5/19/21.
 */

internal fun Color.skia(): Int = (((opacity * 0xFF).toUInt() shl 24) + (red.toUInt() shl 16) + (green.toUInt() shl 8) + blue.toUInt()).toInt()

internal fun Rectangle.skija() = Rect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
internal fun Rectangle.rrect(radius: Float) = RRect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius)

internal fun Paint.skija(): org.jetbrains.skija.Paint {
    val result = org.jetbrains.skija.Paint()

    when (this) {
        is ColorPaint -> result.color = this.color.skia()
    }

    return result
}

internal fun Stroke.skija(): org.jetbrains.skija.Paint = fill.skija().also {
    it.setStroke(true)
    it.strokeWidth = thickness.toFloat()
    dashes?.let { dashes ->
        it.pathEffect = PathEffect.makeDash(dashes.map { it.toFloat() }.toFloatArray(), dashOffset.toFloat())
    }
}