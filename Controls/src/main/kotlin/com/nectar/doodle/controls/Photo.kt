package com.nectar.doodle.controls

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.ImageFactory

/**
 * Created by Nicholas Eddy on 2/14/18.
 */
class Photo private constructor(private var image: Image?): Gizmo() {

    constructor(image: Image): this(image as Image?)

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
            canvas.image(it, bounds.atOrigin())
        }
    }
}