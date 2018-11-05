package com.nectar.doodle.controls

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.ImageFactory

/**
 * Created by Nicholas Eddy on 2/14/18.
 */
class Photo private constructor(private var image: Image?): View() {

    constructor(imageFactory: ImageFactory, source: String): this(null) {
        imageFactory.load(source) {
            image     = it
            size      = it.size
            idealSize = it.size

            rerender()
        }
    }

    override fun render(canvas: Canvas) {
        image?.let {
            canvas.image(it, bounds.atOrigin)
        }
    }

    companion object {
        operator fun invoke(image: Image) = Photo(image)
    }
}