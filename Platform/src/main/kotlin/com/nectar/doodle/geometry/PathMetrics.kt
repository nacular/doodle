package com.nectar.doodle.geometry

/**
 * Created by Nicholas Eddy on 12/4/17.
 */
interface PathMetrics {
    fun width (path: Path): Double
    fun height(path: Path): Double

    fun size(path: Path) = Size(width(path), height(path))
}