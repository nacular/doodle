package io.nacular.doodle.skia

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.toPath
import org.jetbrains.skija.Image
import org.jetbrains.skija.ImageInfo
import org.jetbrains.skija.Matrix33
import org.jetbrains.skija.RRect
import org.jetbrains.skija.Rect
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


internal fun Color.skija(): Int = (((opacity * 0xFF).toUInt() shl 24) + (red.toUInt() shl 16) + (green.toUInt() shl 8) + blue.toUInt()).toInt()

internal fun Rectangle.skija() = Rect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
internal fun Rectangle.rrect(radius: Float) = RRect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius)

internal fun Polygon.skija() = toPath().skija()

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

private class CustomByteArrayOutputStream: ByteArrayOutputStream() {
    val buffer: ByteArray get() = super.buf
}

internal fun BufferedImage.toImage(): Image {
    if (false) {
    val outputStream = CustomByteArrayOutputStream()

    ImageIO.write(this, "png", outputStream)

    return Image.makeFromEncoded(outputStream.buffer)}

// FIXME: Optimize
    return Image.makeRaster(ImageInfo.makeN32Premul(width, height), (data.dataBuffer as DataBufferInt).data.toByteArray(), width * 4L)
//    return Image.makeFromEncoded((raster.dataBuffer as DataBufferByte).data)
}

private fun IntArray.toByteArray(): ByteArray {
    val byteStream = ByteArrayOutputStream()

    forEach {
        byteStream.write((it shr 16) and 0xff) // R
        byteStream.write((it shr  8) and 0xff) // G
        byteStream.write((it       ) and 0xff) // B
        byteStream.write((it shr 24) and 0xff) // A
    }

    return byteStream.toByteArray()
}