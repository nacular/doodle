package io.nacular.doodle.image.impl

import io.nacular.doodle.dom.HTMLImageElement
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image

/**
 * Created by Nicholas Eddy on 11/17/17.
 */
internal class ImageImpl(val image: HTMLImageElement): Image {
    override val size        = Size(image.width, image.height)
    override val source      = image.src
    override val description = image.alt

    override fun hashCode() = image.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other      ) return true
        if (other !is ImageImpl ) return false
        if (image != other.image) return false

        return true
    }
}