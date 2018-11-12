package com.nectar.doodle.image

/**
 * Created by Nicholas Eddy on 11/15/17.
 */
interface ImageLoader {
    suspend fun load  (source: String): Image
            fun unload(image : Image )
}