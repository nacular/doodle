package com.nectar.doodle.image

/**
 * Created by Nicholas Eddy on 11/15/17.
 */
interface ImageFactory {
    // TODO: Use coroutines instead
//    suspend fun load  (source: String): Image
//    suspend fun unload(image : Image )

    fun load(source: String, completed: (Image) -> Unit) = load(source, completed) {}

    fun load(source: String, completed: (Image) -> Unit, error: (Throwable) -> Unit)

    fun unload(image: Image)
}