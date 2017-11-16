package com.nectar.doodle.image

/**
 * Created by Nicholas Eddy on 11/15/17.
 */
interface ImageFactory {
    // TODO: Use coroutines instead
//    suspend fun load(url: String): Image

    fun load(url: String, result: (Image) -> Unit)
}