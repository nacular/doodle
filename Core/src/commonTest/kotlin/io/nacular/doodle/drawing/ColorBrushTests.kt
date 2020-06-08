package io.nacular.doodle.drawing

import io.nacular.doodle.JsName
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.Transparent
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class ColorBrushTests {
    @Test @JsName("defaultsCorrect")
    fun `defaults correct`() {
        ColorBrush(Black).apply {
            expect(Black) { color   }
            expect(true ) { visible }
        }
    }

    @Test @JsName("visibilityCorrect")
    fun `visibility correct`() {
        listOf(
            ColorBrush(color = Red             ) to true,
            ColorBrush(color = Transparent     ) to false,
            ColorBrush(color = Green opacity 0f) to false
        ).forEach {
            expect(it.second) { it.first.visible }
        }
    }

    @Test @JsName("equalsWorks")
    fun `equals works`() {
        listOf(
            ColorBrush(color = Red             ).let { it to it }                          to true,
            ColorBrush(color = Red             ) to ColorBrush(color = Red               ) to true,
            ColorBrush(color = Green opacity 0f) to ColorBrush(color = Green opacity 0.2f) to false,
            ColorBrush(color = Green opacity 0f) to "hello"                                to false
        ).forEach {
            expect(it.second) { it.first.first == it.first.second }
        }
    }
}