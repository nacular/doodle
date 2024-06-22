package io.nacular.doodle.geometry

import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/20/18.
 */
class SizeTests {
    @Test
    fun `default == empty`() = expect(Size.Empty) { Size(0, 0) }

    @Test
    fun `negative side goes to 0`() {
        expect(Size(0, 1)) { Size(width = -20.0, height =   1.0) }
        expect(Size(1, 0)) { Size(width =   1.0, height = -20.0) }
    }

    @Test
    fun `empty has area 0`() = expect(0.0) { Size.Empty.area }

    @Test
    fun `zero side has area 0`() = expect(0.0) { Size(width = 0.0, height = 100.0).area }

    @Test
    fun `area works`() = listOf(10 by 3 to 30, 0 by 56 to 0, 5 by 5 to 25).forEach {
        expect(it.second.toDouble()) { it.first.area }
    }

    @Test
    fun `width height work`() = listOf(10 to 3, 0 to 56, 5 to 5).forEach {
        val rect = it.first by it.second

        expect(it.first.toDouble()) { rect.width }
        expect(it.second.toDouble()) { rect.height }
    }

    @Test
    fun multiply() = listOf(
            (10 by 3) to  0.0 to Size.Empty,
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

    private infix fun Number.by(that: Number) = Size(width = this.toDouble(), height = that.toDouble())
}