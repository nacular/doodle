package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image


/**
 * A [Brush] that produces a repeating fill with the given [Image].
 *
 * @author Nicholas Eddy
 *
 * @property image The image to fill with
 * @property size Size to draw the image when repeating
 * @property opacity The opacity to draw the image with when repeating
 *
 * @constructor
 * @param image The image to fill with
 * @param size Size to draw the image when repeating
 * @param opacity The opacity to draw the image with when repeating
 */
class ImageBrush(
        val image  : Image,
        val size   : Size = image.size,
        val opacity: Float = 1f): Brush() {

    override val visible = opacity > 0 && !size.empty && !image.size.empty
}