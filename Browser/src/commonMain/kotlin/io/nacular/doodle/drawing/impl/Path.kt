package io.nacular.doodle.drawing.impl

import io.nacular.doodle.geometry.Point
import kotlin.js.JsName

internal abstract class Path(
        private val moveTo: String,
        private val lineTo: String,
        private val close : String,
        private val end   : String = "") {

    private val pathString = StringBuilder()

    val data: String get() = "$pathString".trim { it <= ' ' }

    fun moveTo(point: Point) = operation(moveTo, point)
    fun lineTo(point: Point) = operation(lineTo, point)

    fun addPath(vararg points: Point) {
        moveTo(points[0])

        for (i in 1 until points.size) {
            lineTo(points[i])
        }
    }

    @JsName("endFun")
    fun end() = pathString.append(end)

    @JsName("closeFun")
    fun close() {
        pathString.append(close)

        end()
    }

    private fun operation(name: String, point: Point) {
        pathString.append(name)

        pathString.append("${point.x},${point.y}")
    }
}
