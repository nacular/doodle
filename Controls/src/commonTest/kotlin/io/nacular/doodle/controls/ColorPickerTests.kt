package io.nacular.doodle.controls

import io.mockk.mockk
import io.mockk.verify
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.utils.PropertyObserver
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class ColorPickerTests {
    @Test @JsName("initsCorrectly")
    fun `inits correctly`() {
        val picker = ColorPicker(Red)

        expect(Red) { picker.color }
    }

    @Test @JsName("setColorWorks")
    fun `set color works`() {
        listOf(Red, Blue, Black, Transparent).forEach {
            val picker = ColorPicker(White).apply { color = it }

            expect(it) { picker.color }
        }
    }

    @Test @JsName("notifiesColorChange")
    fun `notifies color change`() {
        val picker   = ColorPicker(Red)
        val listener = mockk<PropertyObserver<ColorPicker, Color>>(relaxed = true)

        picker.changed += listener

        picker.color = Blue

        verify(exactly = 1) { listener(picker, Red, Blue) }
    }
}