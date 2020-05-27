package com.nectar.doodle.image

/**
 * Provides a way of loading [Image]s from a given source.
 */
interface ImageLoader {
    /**
     * @param source to load image from
     * @return an image, or null if there is a failure
     */
    suspend fun load(source: String): Image?

    /**
     * Unloads an image that was previously loaded with [load].
     * @param image to unload
     */
    fun unload(image: Image )
}