package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.image.Image


class TextureBrush private constructor(
        val image          : Image,
        val destinationRect: Rectangle,
        val opacity        : Float): Brush() {

    override val visible = opacity > 0 && !destinationRect.empty && !image.size.empty

    companion object {

        fun create(image: Image, opacity: Float = 1.0f): TextureBrush {
            return create(image, Rectangle.create(image.size.width, image.size.height), opacity)
        }

        fun create(image: Image, destination: Rectangle, opacity: Float = 1.0f): TextureBrush {
            return TextureBrush(image, destination, opacity)
        }
    }
}
