package com.nectar.doodle.geometry

/**
 * Represents a path-command string as defined by: https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#Path_commands
 */
interface Path {
    /** command string */
    val data: String
}

/**
 * Provides a way to create [Path]s programmatically.
 */
interface PathBuilder {
    /**
     * Draws a line from the current point to this one.
     *
     * @param point to end at
     */
    infix fun lineTo(point: Point): PathBuilder

    /**
     * Draws a cubic Bézier curve from the current point to this one.
     *
     * @param point to end at
     * @param firstHandle location of the first control point
     * @param secondHandle location of th second control point
     */
    fun cubicTo(point: Point, firstHandle: Point, secondHandle: Point): PathBuilder

    /**
     * Draws a quadratic Bézier curve from the current point to this one.
     *
     * @param point to end at
     * @param handle location of the control point
     */
    fun quadraticTo(point: Point, handle: Point): PathBuilder

    /** Closes the path. */
    fun close(): Path
}

/**
 * Creates a Path from the path data string.
 *
 * @param data conforming to https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#Path_commands
 * @return the path, or `null`
 */
fun path(data: String): Path? = PathImpl(data)

/**
 * Creates a Path at the given point and a builder to further define it.
 *
 * @param from the starting point of the path
 * @return a builder to continue defining the path
 */
fun path(from: Point): PathBuilder = PathBuilderImpl(from)

fun Polygon.toPath(): Path = PathBuilderImpl(points[0]).apply {
    points.subList(1, points.size).forEach {
        lineTo(it)
    }
}.close()


private class PathImpl(override val data: String): Path {
    override fun toString() = data

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode() = data.hashCode()
}

private class PathBuilderImpl(start: Point): PathBuilder {
    private var data = "M${start.x},${start.y}"

    override fun lineTo(point: Point) = this.also {
        data += "L${point.x},${point.y}"
    }

    override fun cubicTo(point: Point, firstHandle: Point, secondHandle: Point) = this.also {
        data += "C${firstHandle.x},${firstHandle.y} ${secondHandle.x},${secondHandle.y} ${point.x},${point.y}"
    }

    override fun quadraticTo(point: Point, handle: Point) = this.also {
        data += "Q${handle.x},${handle.y} ${point.x},${point.y}"
    }

    override fun close(): Path = PathImpl(data + "Z")
}