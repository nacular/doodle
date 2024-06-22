package io.nacular.doodle.image

import io.nacular.doodle.geometry.Size


/**
 * An image that has been loaded into the application.
 */
public interface Image {
    /** The width, height of the image */
    public val size: Size

    /** The source of the image */
    public val source: String

    /** Description of the image */
    public val description: String
}

public inline val Image.width : Double get () = size.width
public inline val Image.height: Double get() = size.height