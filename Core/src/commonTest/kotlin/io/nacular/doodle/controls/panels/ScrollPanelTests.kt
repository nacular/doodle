package io.nacular.doodle.controls.panels

import io.nacular.doodle.JsName
import io.nacular.doodle.core.Box
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
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
        val behavior = behavior()
        val panel    = panel(behavior)

        listOf(
            Point( 10.0, 10.0),
            Point(-10.0, 10.0)
        ).forEach {
            expect(it) {
                panel.scrollTo(it)
                panel.scroll
            }

            verify { behavior.scrollTo(it) }
        }
    }

    @Test @JsName("scrollBy")
    fun `scroll by`() {
        val behavior = behavior()
        val start    = Point(5.0, 5.0)

        listOf(
            Point( 10.0, 10.0),
            Point(-10.0,  0.0)
        ).forEach { point ->
            panel(behavior).apply {
                scrollTo(start)

                expect(start + point) {
                    scrollBy(point)
                    scroll
                }
            }

            verify { behavior.scrollTo(start + point) }
        }
    }

    @Test @JsName("scrollToVisiblePoint")
    fun `scroll to visible point`() {
        val behavior = behavior()
        val start    = Point(5.0, 5.0)

        mapOf(
            Point( 100.0, 10.0) to Point( 90.0, 5.0),
            Point( -10.0,  0.0) to Point(-10.0, 0.0)
        ).forEach { (target, result) ->
            panel(behavior).apply {
                scrollTo(start)

                expect(result) {
                    scrollToVisible(target)
                    scroll
                }
            }

            verify { behavior.scrollTo(result) }
        }
    }

    private fun panel(behavior: ScrollPanelBehavior = behavior()): ScrollPanel {
        val content = Box().apply { size = Size(100.0, 100.0) }

        return ScrollPanel(content).apply {
            this.behavior = behavior
            size          = Size(10.0, 10.0)
        }
    }

    private fun behavior(): ScrollPanelBehavior {
        val behavior = mockk<ScrollPanelBehavior>()

        val point    = CapturingSlot<Point>()
        val onScroll = CapturingSlot<(Point) -> Unit>()

        every { behavior.onScroll = capture(onScroll) } just Runs
        every { behavior.scrollTo(capture(point)) } answers {
            onScroll.captured(point.captured)
        }

        return behavior
    }
}