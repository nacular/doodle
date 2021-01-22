package io.nacular.doodle.controls.icons

import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image

/**
 * Created by Nicholas Eddy on 8/13/19.
 */
public class ImageIcon<T: View>(private val image: Image): Icon<T> {
    override fun size(view: T): Size = image.size

    override fun render(view: T, canvas: Canvas, at: Point) {
        canvas.image(image, Rectangle(position = at, size = size(view)))
    }
}
