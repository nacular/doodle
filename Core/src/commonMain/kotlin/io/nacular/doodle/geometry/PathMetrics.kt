package io.nacular.doodle.geometry

/**
 * Provides a mechanism to measure the size of a [Path].
 */
public interface PathMetrics {
    /**
     * @param path to measure
     * @return the bounding width of the path
     */
    public fun width(path: Path): Double

    /**
     * @param path to measure
     * @return the bounding height of the path
     */
    public fun height(path: Path): Double

    /**
     * @param path to measure
     * @return the bounding size of the path
     */
    public fun size(path: Path): Size = Size(width(path), height(path))
}