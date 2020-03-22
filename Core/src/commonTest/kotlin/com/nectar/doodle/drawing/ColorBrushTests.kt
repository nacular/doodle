package com.nectar.doodle.drawing

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.Color.Companion.transparent
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class ColorBrushTests {
    @Test @JsName("defaultsCorrect")
    fun `defaults correct`() {
        ColorBrush(black).apply {
            expect(black) { color     }
            expect(true ) { visible   }
        }
    }

    @Test @JsName("visibilityCorrect")
    fun `visibility correct`() {
        listOf(
            ColorBrush(color = red             ) to true,
            ColorBrush(color = transparent     ) to false,
            ColorBrush(color = green opacity 0f) to false
        ).forEach {
            expect(it.second) { it.first.visible }
        }
    }

    @Test @JsName("equalsWorks")
    fun `equals works`() {
        listOf(
            ColorBrush(color = red             ).let { it to it }                          to true,
            ColorBrush(color = red             ) to ColorBrush(color = red               ) to true,
            ColorBrush(color = green opacity 0f) to ColorBrush(color = green opacity 0.2f) to false,
            ColorBrush(color = green opacity 0f) to "hello"                                to false
        ).forEach {
            expect(it.second) { it.first.first == it.first.second }
        }
    }
}