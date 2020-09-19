package io.nacular.doodle.geometry

/**
 * Provides a mechanism to measure the size of a [Path].
 */
interface PathMetrics {
    /**
     * @param path to measure
     * @return the bounding width of the path
     */
    fun width(path: Path): Double

    /**
     * @param path to measure
     * @return the bounding height of the path
     */
    fun height(path: Path): Double

    /**
     * @param path to measure
     * @return the bounding size of the path
     */
    fun size(path: Path) = Size(width(path), height(path))
}