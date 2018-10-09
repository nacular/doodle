package com.nectar.doodle.geometry.impl

import com.nectar.doodle.geometry.Path
import com.nectar.doodle.geometry.PathFactory
import com.nectar.doodle.geometry.PathMetrics
import com.nectar.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 11/22/17.
 */
private class PathImpl(override val data: String, private val pathMetrics: PathMetrics): Path {
    override val size: Size by lazy {
        pathMetrics.size(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode() = data.hashCode()
}

class PathFactoryImpl(private val pathMetrics: PathMetrics): PathFactory {
    override operator fun invoke(data: String): Path? = PathImpl(data, pathMetrics)
}