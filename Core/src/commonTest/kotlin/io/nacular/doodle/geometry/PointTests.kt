package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Point.Companion.Origin
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect


/**
 * Created by Nicholas Eddy on 2/20/18.
 */
class PointTests {
    @Test @JsName("defaultIsOrigin")
    fun `default == origin`() = expect(Origin) { Point(0, 0) }

    @Test @JsName("xYWork")
    fun `x y work`() = listOf(10 to 3, 0 to 56, 5 to 5).forEach {
        val rect = it.first by it.second

        expect(it.first.toDouble ()) { rect.x }
        expect(it.second.toDouble()) { rect.y }
    }

    @Test @JsName("multiplyDouble")
    fun `multiply double`() = listOf(
            (10 by 3) to  0.0 to Origin,
            (10 by 3) to  2.0 to ( 20 by  6),
            (10 by 3) to -2.0 to (-20 by -6),
            (10 by 4) to  0.5 to (  5  by 2)
    ).forEach {
        expect(it.second) { it.first.first * it.first.second }
    }

    @Test @JsName("multiplyFloat")
    fun `multiply float`() = listOf(
            (10 by 3) to  0.0f to Origin,
            (10 by 3) to  2.0f to ( 20 by  6),
            (10 by 3) to -2.0f to (-20 by -6),
            (10 by 4) to  0.5f to (  5  by 2)
    ).forEach {
        expect(it.second) { it.first.first * it.first.second }
    }

    @Test @JsName("multiplyInt")
    fun `multiply int`() = listOf(
            (10 by 3) to  0 to Origin,
            (10 by 3) to  2 to ( 20 by  6),
            (10 by 3) to -2 to (-20 by -6),
            (10 by 4) to  3 to (30  by 12)
    ).forEach {
        expect(it.second) { it.first.first * it.first.second }
    }

    @Test
    fun divide() = listOf(
            (10 by 3) to 1.0 to (10 by 3  ),
            (10 by 3) to 2.0 to (5  by 1.5),
            (10 by 4) to 0.5 to (20 by 8  )
    ).forEach {
        expect(it.second) { it.first.first / it.first.second }
    }

    @Test @JsName("divideFloat")
    fun `divide float`() = listOf(
            (10 by 3) to 1.0f to (10 by 3  ),
            (10 by 3) to 2.0f to (5  by 1.5),
            (10 by 4) to 0.5f to (20 by 8  )
    ).forEach {
        expect(it.second) { it.first.first / it.first.second }
    }

    @Test @JsName("divideInt")
    fun `divide int`() = listOf(
            (10 by 3) to 1 to (10 by   3),
            (10 by 3) to 2 to (5  by 1.5),
            ( 9 by 6) to 3 to (3  by   2)
    ).forEach {
        expect(it.second) { it.first.first / it.first.second }
    }

    @Test
    fun negate() = listOf(
            ( 10 by  3) to (-10 by -3),
            (-10 by  3) to ( 10 by -3),
            (-10 by -4) to ( 10 by  4),
            ( 12 by -4) to (-12 by  4)
    ).forEach {
        expect(it.second) { -it.first }
    }

    @Test @JsName("distanceWorks")
    fun `distance works`() = listOf(
            (0 by 0) to ( 0 by  0) to 0.0,
            (0 by 0) to ( 3 by  4) to 5.0,
            (0 by 0) to ( 4 by  3) to 5.0,
            (0 by 0) to (-3 by  4) to 5.0,
            (0 by 0) to ( 4 by -3) to 5.0
    ).forEach { (points, distance) ->
        expect(distance) {
            points.first distanceFrom points.second
        }
    }
}

private infix fun Number.by(that: Number) = Point(x = this.toDouble(), y = that.toDouble())
