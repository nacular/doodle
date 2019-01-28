package com.nectar.doodle.controls.panels

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Box
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 5/15/18.
 */


class ScrollPanelTests {
    @Test @JsName("scrollToPoint")
    fun `scroll to point`() {
        val renderer = renderer()
        val panel    = panel(renderer)

        listOf(
            Point( 10.0, 10.0),
            Point(-10.0, 10.0)
        ).forEach {
            expect(it) {
                panel.scrollTo(it)
                panel.scroll
            }

            verify { renderer.scrollTo(it) }
        }
    }

    @Test @JsName("scrollBy")
    fun `scroll by`() {
        val renderer = renderer()
        val start    = Point(5.0, 5.0)

        listOf(
            Point( 10.0, 10.0),
            Point(-10.0,  0.0)
        ).forEach { point ->
            panel(renderer).apply {
                scrollTo(start)

                expect(start + point) {
                    scrollBy(point)
                    scroll
                }
            }

            verify { renderer.scrollTo(start + point) }
        }
    }

    @Test @JsName("scrollToVisiblePoint")
    fun `scroll to visible point`() {
        val renderer = renderer()
        val start    = Point(5.0, 5.0)

        mapOf(
            Point( 100.0, 10.0) to Point( 90.0, 5.0),
            Point( -10.0,  0.0) to Point(-10.0, 0.0)
        ).forEach { (target, result) ->
            panel(renderer).apply {
                scrollTo(start)

                expect(result) {
                    scrollToVisible(target)
                    scroll
                }
            }

            verify { renderer.scrollTo(result) }
        }
    }

    private fun panel(renderer: ScrollPanelRenderer = renderer()): ScrollPanel {
        val content = Box().apply { size = Size(100.0, 100.0) }

        return ScrollPanel(content).apply {
            this.renderer = renderer
            size          = Size(10.0, 10.0)
        }
    }

    private fun renderer(): ScrollPanelRenderer {
        val renderer = mockk<ScrollPanelRenderer>(relaxed = true)

        val point    = CapturingSlot<Point>()
        val onScroll = CapturingSlot<(Point) -> Unit>()

        every { renderer.onScroll = capture(onScroll) } just Runs
        every { renderer.scrollTo(capture(point)) } answers {
            onScroll.captured(point.captured)
        }

        return renderer
    }
}