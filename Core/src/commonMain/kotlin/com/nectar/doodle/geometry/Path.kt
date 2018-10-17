package com.nectar.doodle.geometry

/**
 * Created by Nicholas Eddy on 11/22/17.
 */

interface Path {
    val data: String
    val size: Size
    val width  get() = size.width
    val height get() = size.height
}

interface PathFactory {
    operator fun invoke(data: String): Path?
}