package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image


class TextureBrush(
        val image          : Image,
        val destinationRect: Rectangle,
        val opacity        : Float = 1f): Brush() {

    constructor(image: Image, opacity: Float = 1f): this(image, Rectangle(size = image.size), opacity)

    override val visible = opacity > 0 && !destinationRect.empty && !image.size.empty
}

class ImageBrush(val size: Size, val fill: Canvas.() -> Unit): Brush() {
    override val visible = true
}