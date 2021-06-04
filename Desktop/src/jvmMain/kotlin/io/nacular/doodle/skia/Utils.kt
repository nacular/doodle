package io.nacular.doodle.skia

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Renderer.FillRule.EvenOdd
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import org.jetbrains.skija.Matrix33
import org.jetbrains.skija.PathFillMode.EVEN_ODD
import org.jetbrains.skija.PathFillMode.WINDING
import org.jetbrains.skija.RRect
import org.jetbrains.skija.Rect

/**
 * Created by Nicholas Eddy on 5/19/21.
 */

internal fun Color.skia(): Int = (((opacity * 0xFF).toUInt() shl 24) + (red.toUInt() shl 16) + (green.toUInt() shl 8) + blue.toUInt()).toInt()

internal fun Rectangle.skija() = Rect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
internal fun Rectangle.rrect(radius: Float) = RRect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius)

internal fun Point.skija() = org.jetbrains.skija.Point(x.toFloat(), y.toFloat())

internal fun Path.skija() = org.jetbrains.skija.Path.makeFromSVGString(data)

internal fun AffineTransform.skija() = Matrix33(
        scaleX.toFloat    (),
        shearX.toFloat    (),
        translateX.toFloat(),
        shearY.toFloat    (),
        scaleY.toFloat    (),
        translateY.toFloat(),
        0f,
        0f,
        1f
)