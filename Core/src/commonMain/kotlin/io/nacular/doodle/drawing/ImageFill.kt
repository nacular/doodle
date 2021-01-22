package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image


/**
 * A [Fill] that produces a repeating fill with the given [Image].
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
public class ImageFill(public val image: Image, public val size: Size = image.size, public val opacity: Float = 1f): Fill() {
    public override val visible: Boolean = opacity > 0 && !size.empty && !image.size.empty
}