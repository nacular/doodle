package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.image.Image


class TextureBrush(
        val image          : Image,
        val destinationRect: Rectangle,
        val opacity        : Float = 1f): Brush() {

    constructor(image: Image, opacity: Float = 1f): this(image, Rectangle(size = image.size), opacity)

    override val visible = opacity > 0 && !destinationRect.empty && !image.size.empty
}
