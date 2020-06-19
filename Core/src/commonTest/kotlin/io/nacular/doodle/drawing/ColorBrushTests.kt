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
class ColorFillTests {
    @Test @JsName("defaultsCorrect")
    fun `defaults correct`() {
        ColorFill(Black).apply {
            expect(Black) { color   }
            expect(true ) { visible }
        }
    }

    @Test @JsName("visibilityCorrect")
    fun `visibility correct`() {
        listOf(
            ColorFill(color = Red             ) to true,
            ColorFill(color = Transparent     ) to false,
            ColorFill(color = Green opacity 0f) to false
        ).forEach {
            expect(it.second) { it.first.visible }
        }
    }

    @Test @JsName("equalsWorks")
    fun `equals works`() {
        listOf(
            ColorFill(color = Red             ).let { it to it }                          to true,
            ColorFill(color = Red             ) to ColorFill(color = Red               ) to true,
            ColorFill(color = Green opacity 0f) to ColorFill(color = Green opacity 0.2f) to false,
            ColorFill(color = Green opacity 0f) to "hello"                                to false
        ).forEach {
            expect(it.second) { it.first.first == it.first.second }
        }
    }
}