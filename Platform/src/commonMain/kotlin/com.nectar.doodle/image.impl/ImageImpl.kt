package com.nectar.doodle.image.impl

import com.nectar.doodle.HTMLImageElement
import com.nectar.doodle.dom.height
import com.nectar.doodle.dom.width
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image

/**
 * Created by Nicholas Eddy on 11/17/17.
 */
internal class ImageImpl(val image: HTMLImageElement): Image {
    override val size   = Size(image.width, image.height)
    override val source = image.src

    override fun hashCode() = image.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other     ) return true
        if (other !is ImageImpl) return false

        if (image != other.image) return false

        return true
    }
}