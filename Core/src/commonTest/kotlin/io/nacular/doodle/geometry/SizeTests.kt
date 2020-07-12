package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Size.Companion.Empty
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect


/**
 * Created by Nicholas Eddy on 2/20/18.
 */
class SizeTests {
    @Test @JsName("defaultIsEmpty")
    fun `default == empty`() = expect(Empty) { Size(0, 0) }

    @Test @JsName("negativeSideFails")
    fun `negative side fails`() {
        assertFailsWith(IllegalArgumentException::class) { Size(width = -20.0, height =   1.0) }
        assertFailsWith(IllegalArgumentException::class) { Size(width =   1.0, height = -20.0) }
    }

    @Test @JsName("emptyHasSizeZero")
    fun `empty has area 0`() = expect(0.0) { Empty.area }

    @Test @JsName("zeroSideHasSizeZero")
    fun `zero side has area 0`() = expect(0.0) { Size(width = 0.0, height = 100.0).area }

    @Test @JsName("areaWorks")
    fun `area works`() = listOf(10 by 3 to 30, 0 by 56 to 0, 5 by 5 to 25).forEach {
        expect(it.second.toDouble()) { it.first.area }
    }

    @Test @JsName("widthHeightWork")
    fun `width height work`() = listOf(10 to 3, 0 to 56, 5 to 5).forEach {
        val rect = it.first by it.second

        expect(it.first.toDouble ()) { rect.width  }
        expect(it.second.toDouble()) { rect.height }
    }

    @Test
    fun multiply() = listOf(
            (10 by 3) to  0.0 to Empty,
            (10 by 3) to  2.0 to (20 by 6),
            (10 by 4) to  0.5 to (5  by 2)
    ).forEach {
        expect(it.second) { it.first.first * it.first.second }
    }

    @Test
    fun divide() = listOf(
            (10 by 3) to  1.0 to (10 by 3  ),
            (10 by 3) to  2.0 to (5  by 1.5),
            (10 by 4) to  0.5 to (20 by 8  )
    ).forEach {
        expect(it.second) { it.first.first / it.first.second }
    }
}

private infix fun Number.by(that: Number) = Size(width = this.toDouble(), height = that.toDouble())
