package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image


/**
 * A [Paint] that produces a repeating fill with the given [Image].
 *
 * @author Nicholas Eddy
 *
 * @property image The image to paint with
 * @property size Size to draw the image when repeating
 * @property opacity The opacity to draw the image with when repeating
 *
 * @constructor
 * @param image The image to paint with
 * @param size Size to draw the image when repeating
 * @param opacity The opacity to draw the image with when repeating
 */
public class ImagePaint(public val image: Image, public val size: Size = image.size, public val opacity: Float = 1f): Paint() {
    public override val visible: Boolean = opacity > 0 && !size.empty && !image.size.empty
}

@Deprecated("Use ImagePaint instead", replaceWith = ReplaceWith("ImagePaint"))
public typealias ImageFill = ImagePaint