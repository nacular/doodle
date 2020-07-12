package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Point.Companion.Origin
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

    @Test
    fun multiply() = listOf(
            (10 by 3) to  0.0 to Origin,
            (10 by 3) to  2.0 to ( 20 by  6),
            (10 by 3) to -2.0 to (-20 by -6),
            (10 by 4) to  0.5 to (  5  by 2)
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

    @Test
    fun negate() = listOf(
            ( 10 by  3) to (-10 by -3),
            (-10 by  3) to ( 10 by -3),
            (-10 by -4) to ( 10 by  4),
            ( 12 by -4) to (-12 by  4)
    ).forEach {
        expect(it.second) { -it.first }
    }
}

private infix fun Number.by(that: Number) = Point(x = this.toDouble(), y = that.toDouble())
