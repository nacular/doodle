package io.nacular.doodle.image.impl

import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import org.jetbrains.skija.Image as SkijaImage

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
internal class ImageImpl(val skiaImage: SkijaImage, override val source: String): Image {
    override val size: Size by lazy { Size(skiaImage.imageInfo.width, skiaImage.imageInfo.height) }
}