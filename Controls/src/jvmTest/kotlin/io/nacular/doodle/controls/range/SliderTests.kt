package io.nacular.doodle.controls.range

import JsName
import org.junit.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 11/23/21.
 */
class SliderTests {
    @Test @JsName("snapsToTicks")
    fun `snaps to ticks`() {
        val slider = Slider(28.0 .. 31.0)

        slider.ticks       = 4
        slider.snapToTicks = true
        slider.value       = 31.0

        expect(31.0) { slider.value }
    }

    @Test @JsName("snapsToTicksInt")
    fun `snaps to ticks int`() {
        val slider = Slider(28 .. 31)

        slider.ticks       = 4
        slider.snapToTicks = true
        slider.value       = 31

        expect(31) { slider.value }
    }

    @Test @JsName("canChangeFromZero")
    fun `can change from 0`() {
        val slider = Slider(0.0f .. 1.0f)

        slider.value = 0.0f

        expect(0.0f) { slider.value }

        slider.value = 0.5f

        expect(0.5f) { slider.value }
    }
}