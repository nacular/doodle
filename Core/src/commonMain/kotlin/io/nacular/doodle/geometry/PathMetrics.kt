package io.nacular.doodle.geometry

/**
 * Provides a mechanism to measure the size of a [Path].
 */
public interface PathMetrics {
    /**
     * @param path to measure
     * @return the bounding width of the path
     */
    // TODO: Add support for Stroke
    public fun width(path: Path): Double

    /**
     * @param path to measure
     * @return the bounding height of the path
     */
    // TODO: Add support for Stroke
    public fun height(path: Path): Double

    /**
     * @param path to measure
     * @return the bounding size of the path
     */
    // TODO: Add support for Stroke
    public fun size(path: Path): Size

    /**
     * @param path to measure
     * @return the bounding rectangle of the path
     */
    // TODO: Add support for Stroke
    public fun bounds(path: Path): Rectangle

    /**
     * @param path to measure
     * @param stroke the path will have
     * @return the the linear length of a path (i.e. the perimeter for a circle)
     */
    // TODO: Add support for Stroke
    public fun length(path: Path): Double
}