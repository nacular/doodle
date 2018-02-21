package com.nectar.doodle.geometry

import com.nectar.doodle.JsName
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle.Companion.Empty
import com.nectar.doodle.layout.Insets
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect


/**
 * Created by Nicholas Eddy on 2/20/18.
 */
class RectangleTests {
    @Test @JsName("defaultIsEmpty")
    fun `default == empty`() = expect(Empty) { Rectangle() }

    @Test @JsName("defaultsToOrigin")
    fun `defaults to origin`() = expect(Origin) { Rectangle(width = 1.0, height = 2.0).position }

    @Test @JsName("negativeSideFails")
    fun `negative side fails`() {
        assertFailsWith(IllegalArgumentException::class) { Rectangle(width = -20.0, height =   1.0) }
        assertFailsWith(IllegalArgumentException::class) { Rectangle(width =   1.0, height = -20.0) }
    }

    @Test @JsName("emptyHasSizeZero")
    fun `empty has area 0`() = expect(0.0) { Empty.area }

    @Test @JsName("zeroSideHasSizeZero")
    fun `zero side has area 0`() = expect(0.0) { Rectangle(width = 0.0, height = 100.0).area }

    @Test @JsName("areaWorks")
    fun `area works`() = listOf(10 by 3 to 30, 0 by 56 to 0, 5 by 5 to 25).forEach {
        expect(it.second.toDouble()) { it.first.area }
    }

    @Test @JsName("widthHeightWork")
    fun `width height work`() = listOf(10 to 3, 0 to 56, 5 to 5).forEach {
        val rectangle = it.first by it.second

        expect(it.first.toDouble ()) { rectangle.width  }
        expect(it.second.toDouble()) { rectangle.height }
    }

    @Test @JsName("insetWorks")
    fun `inset works`() = listOf(10 to 3, 0 to 56, 5 to 5).forEach {
        val rect = it.first by it.second

        listOf(Insets.None, Insets(1.0), Insets(top = 1.0), Insets(left = 1.0)).forEach {
            expect(Rectangle(rect.x + it.left, rect.y + it.top, max(0.0, rect.width - (it.left + it.right)), max(0.0, rect.height - (it.top + it.bottom)))) {
                rect.inset(it)
            }
        }
    }
}

private infix fun Int.by   (value: Int   ) = Rectangle(width = this.toDouble(), height = value.toDouble())
//private infix fun Double.by(value: Double) = Rectangle(width = this,            height = value           )