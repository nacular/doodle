package com.nectar.doodle.drawing.impl

import com.nectar.doodle.geometry.Point

abstract class Path constructor(
        private val moveTo: String,
        private val lineTo: String,
        private val close : String,
        private val end   : String = "") {

    private val pathString = StringBuilder()

    val data: String get() = pathString.toString().trim { it <= ' ' }

    fun moveTo(point: Point) = operation(moveTo, point)
    fun lineTo(point: Point) = operation(lineTo, point)

    fun addPath(vararg points: Point) {
        moveTo(points[0])

        for (i in 1 until points.size) {
            lineTo(points[i])
        }
    }

    fun end() = pathString.append(end)

    fun close() {
        pathString.append(close)

        end()
    }

    private fun operation(name: String, point: Point) {
        pathString.append(name)

        pathString.append("${point.x},${point.y}")
    }
}
