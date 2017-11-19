package com.nectar.doodle.image.impl

import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image
import org.w3c.dom.HTMLImageElement

/**
 * Created by Nicholas Eddy on 11/17/17.
 */
internal class ImageImpl(val image: HTMLImageElement) : Image {
    override val size   = Size(image.width.toDouble(), image.height.toDouble())
    override val source = image.src
}