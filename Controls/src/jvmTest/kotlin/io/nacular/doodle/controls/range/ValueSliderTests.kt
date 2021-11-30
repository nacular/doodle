package io.nacular.doodle.controls.range

import JsName
import org.junit.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 11/23/21.
 */
class ValueSliderTests {
    @Test @JsName("snapsToTicks")
    fun `snaps to ticks`() {
        val slider = object: ValueSlider2<Double>(28.0 .. 31.0) {
            override fun changed(old: Double, new: Double) {}
        }

        slider.ticks       = 4
        slider.snapToTicks = true
        slider.value       = 31.0

        expect(31.0) { slider.value }
    }

    @Test @JsName("snapsToTicks2")
    fun `snaps to ticks 2`() {
        val slider = object: ValueSlider2<Int>(28 .. 31) {
            override fun changed(old: Int, new: Int) {}
        }

        slider.ticks       = 4
        slider.snapToTicks = true
        slider.value       = 31

        expect(31) { slider.value }
    }
}