package io.nacular.doodle.image.impl

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Rect
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.Image as SkijaImage

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
internal class ImageImpl(val skiaImage: SkijaImage, override val source: String): Image {
    override val size: Size by lazy { Size(skiaImage.imageInfo.width, skiaImage.imageInfo.height) }
}

internal class SvgImage(private val dom: SVGDOM): Image {
    fun render(canvas: Canvas, source: Rect, destination: Rect) {
        canvas.save()
        canvas.translate(destination.left - source.left, destination.top - source.top)
        canvas.scale(destination.width / source.width, destination.height / source.height)
        canvas.clipRect(source)

        dom.render(canvas)

        canvas.restore()
    }

    override val size: Size = dom.root!!.run { Size(width.value, height.value) }
    override val source get() = dom.toString()
}