package com.nectar.doodle.controls.icons

import com.nectar.doodle.core.Icon
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image

/**
 * Created by Nicholas Eddy on 8/13/19.
 */
class ImageIcon<T: View>(private val image: Image) : Icon<T> {
    override val size: Size get() = image.size

    override fun render(view: T, canvas: Canvas, at: Point) {
        canvas.image(image, Rectangle(position = at, size = size))
    }
}
