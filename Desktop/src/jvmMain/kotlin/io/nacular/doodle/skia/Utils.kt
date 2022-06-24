package io.nacular.doodle.skia

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.PatternTransform
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.toPath
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Matrix44
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect


internal fun Color.skia(): Int = (((opacity * 0xFF).toUInt() shl 24) + (red.toUInt() shl 16) + (green.toUInt() shl 8) + blue.toUInt()).toInt()

internal fun Rectangle.skia() = Rect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
internal fun Rectangle.rrect(radius: Float) = RRect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius)

internal fun Polygon.skia() = toPath().skia()

internal fun Point.skia() = org.jetbrains.skia.Point(x.toFloat(), y.toFloat())

internal fun Path.skia() = org.jetbrains.skia.Path.makeFromSVGString(data)

internal fun AffineTransform.skia33() = Matrix33(
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

internal fun PatternTransform.skia33() = Matrix33(
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

internal fun AffineTransform.skia44() = Matrix44(
        m00.toFloat(), m01.toFloat(), m02.toFloat(), m03.toFloat(),
        m10.toFloat(), m11.toFloat(), m12.toFloat(), m13.toFloat(),
        m20.toFloat(), m21.toFloat(), m22.toFloat(), m23.toFloat(),
        0f,            0f,            0f,            1f
)