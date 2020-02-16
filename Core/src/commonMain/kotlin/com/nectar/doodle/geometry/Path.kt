package com.nectar.doodle.geometry

/**
 * Created by Nicholas Eddy on 11/22/17.
 */

interface Path {
    val data: String
}

//interface PathFactory {
//    operator fun invoke(data: String): Path?
//    operator fun invoke(start: Point): PathBuilder
//}

interface PathBuilder {
    infix fun lineTo(point: Point): PathBuilder
    fun cubicTo(point: Point, firstHandle: Point, secondHandle: Point): PathBuilder
    fun quadraticTo(point: Point, handle: Point): PathBuilder

    fun close(): Path
}

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

fun path(data: String ): Path?       = PathImpl(data)
fun path(from: Point  ): PathBuilder = PathBuilderImpl(from)

fun Polygon.toPath(): Path = PathBuilderImpl(points[0]).apply {
    points.subList(1, points.size).forEach {
        lineTo(it)
    }
}.close()

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