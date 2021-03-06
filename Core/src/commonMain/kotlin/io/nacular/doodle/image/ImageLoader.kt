package io.nacular.doodle.image

import io.nacular.doodle.datatransport.LocalFile

/**
 * Provides a way of loading [Image]s from a given source.
 */
public interface ImageLoader {
    /**
     * @param source to load image from
     * @return an image, or null if there is a failure
     */
    public suspend fun load(source: String): Image?

    /**
     * @param file to load image from
     * @return an image, or null if there is a failure
     */
    public suspend fun load(file: LocalFile): Image?

    /**
     * Unloads an image that was previously loaded with [load].
     * @param image to unload
     */
    public fun unload(image: Image)
}