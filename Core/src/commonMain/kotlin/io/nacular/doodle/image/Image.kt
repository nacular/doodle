package io.nacular.doodle.image

import io.nacular.doodle.geometry.Size


/**
 * An image that has been loaded into the application.
 */
interface Image {
    /** The width, height of the image */
    val size: Size

    /** The source of the image */
    val source: String
}