package com.nectar.doodle.controls

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.image.Image

/**
 * A simple wrapper around an [Image]. The image is scaled to fit within the
 * bounds of this Photo when drawn.
 */
class Photo(private var image: Image): View() {
    init {
        size = image.size
    }

    override fun render(canvas: Canvas) {
        canvas.image(image, destination = bounds.atOrigin)
    }
}