package com.nectar.doodle.controls

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.drawing.Color.Companion.blue
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.Color.Companion.transparent
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.utils.PropertyObserver
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 3/21/20.
 */
class ColorPickerTests {
    @Test @JsName("initsCorrectly")
    fun `inits correctly`() {
        val picker = ColorPicker(red)

        expect(red) { picker.color }
    }

    @Test @JsName("setColorWorks")
    fun `set color works`() {
        listOf(red, blue, black, transparent).forEach {
            val picker = ColorPicker(white).apply { color = it }

            expect(it) { picker.color }
        }
    }

    @Test @JsName("notifiesColorChange")
    fun `notifies color change`() {
        val picker   = ColorPicker(red)
        val listener = mockk<PropertyObserver<ColorPicker, Color>>(relaxed = true)

        picker.changed += listener

        picker.color = blue

        verify(exactly = 1) { listener(picker, red, blue) }
    }
}