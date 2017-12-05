package com.nectar.doodle.geometry

/**
 * Created by Nicholas Eddy on 11/22/17.
 */

interface Path {
    val data: String
    val size: Size
}

interface PathFactory {
    fun create(data: String): Path?
}