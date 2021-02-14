package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.Transparent
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class ColorFillTests {
    @Test @JsName("defaultsCorrect")
    fun `defaults correct`() {
        ColorPaint(Black).apply {
            expect(Black) { color   }
            expect(true ) { visible }
        }
    }

    @Test @JsName("visibilityCorrect")
    fun `visibility correct`() {
        listOf(
            ColorPaint(color = Red             ) to true,
            ColorPaint(color = Transparent     ) to false,
            ColorPaint(color = Green opacity 0f) to false
        ).forEach {
            expect(it.second) { it.first.visible }
        }
    }

    @Test @JsName("equalsWorks")
    fun `equals works`() {
        listOf(
            ColorPaint(color = Red             ).let { it to it }                          to true,
            ColorPaint(color = Red             ) to ColorPaint(color = Red               ) to true,
            ColorPaint(color = Green opacity 0f) to ColorPaint(color = Green opacity 0.2f) to false,
            ColorPaint(color = Green opacity 0f) to "hello"                                to false
        ).forEach {
            expect(it.second) { it.first.first == it.first.second }
        }
    }
}