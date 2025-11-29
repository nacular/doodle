package io.nacular.doodle.controls.range

import JsName
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 11/23/21.
 */
class SliderTests {
    @Test @JsName("snapsToMarks")
    fun `snaps to marks`() {
        val slider = Slider(0.0 .. 100.0).apply {
            marker = mockk {
                val progress = slot<Float>()

                every { marks  (0.0 .. 100.0, any()                   ) } returns sequenceOf(0.1f, 0.15f)
                every { nearest(0.0 .. 100.0, any(), capture(progress)) } answers {
                    when {
                        progress.captured <= (0.1f + (0.15f - 0.1f) / 2f) -> 0.1f
                        else -> 0.15f
                    }
                }
            }
            snappingPolicy = alwaysSnap()
        }

        listOf(
             7 to 0.1f  * 100.0,
            14 to 0.15f * 100.0,
            99 to 0.15f * 100.0,
        ).forEach { (attempted, expected) ->
            slider.value = attempted.toDouble()
            expect(expected, "$attempted should be $expected") { slider.value }
        }

        val slider2 = Slider(0f .. 1f).apply {
            marker         = evenMarker(2)
            snappingPolicy = alwaysSnap()
        }

        slider2.value = 0.1f
        expect(0f) { slider2.value }

        slider2.value = 0.27f
        expect(1/3f) { slider2.value }
    }

    @Test @JsName("snapsToMarksInt")
    fun `snaps to marks int`() {
        val slider = Slider(0 .. 100).apply {
            marker = mockk {
                val progress = slot<Float>()

                every { marks  (0 .. 100, any()                   ) } returns sequenceOf(0.1f, 0.15f)
                every { nearest(0 .. 100, any(), capture(progress)) } answers {
                    when {
                        progress.captured <= (0.1f + (0.15f - 0.1f) / 2f) -> 0.1f
                        else -> 0.15f
                    }
                }
            }
            snappingPolicy = alwaysSnap()
        }

        listOf(
            7  to 0.1f  * 100.0,
            14 to 0.15f * 100.0,
            99 to 0.15f * 100.0,
        ).forEach { (attempted, expected) ->
            slider.value = attempted
            expect(expected.toInt(), "$attempted should be $expected") { slider.value }
        }
    }

    @Test @JsName("canChangeFromZero")
    fun `can change from 0`() {
        val slider = Slider(0f .. 1f)

        slider.value = 0f

        expect(0f) { slider.value }

        slider.value = 0.5f

        expect(0.5f) { slider.value }
    }
}