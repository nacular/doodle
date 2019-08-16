package com.nectar.doodle.controls

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.image.Image

/**
 * Created by Nicholas Eddy on 2/14/18.
 */
class Photo(private var image: Image): View() {
    init {
        size = image.size
    }

    override fun render(canvas: Canvas) {
        canvas.image(image, source = bounds.atOrigin)
    }
}