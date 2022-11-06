package io.nacular.doodle.controls.panels

import io.mockk.Called
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.PropertyObserver
import JsName
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 5/15/18.
 */
class ScrollPanelTests {
    @Test @JsName("cannotAssignPanelToItself")
    fun `cannot assign panel to itself`() {
        assertFailsWith<IllegalArgumentException> { panel().let { it.content = it } }
    }

    @Test @JsName("contentsTrackedProperly")
    fun `contents tracked properly`() {
        val panel    = ScrollPanel()
        val content  = mockk<View>()
        val observer = mockk<PropertyObserver<ScrollPanel, View?>>()

        panel.contentChanged += observer
        panel.content         = content

        expect(true) {
            content in panel.children_
        }

        verify(exactly = 1) { observer(panel, null, content) }
    }

    @Test @JsName("contentsRemovedProperly")
    fun `contents removed properly`() {
        val panel    = ScrollPanel()
        val content  = mockk<View>()
        val observer = mockk<PropertyObserver<ScrollPanel, View?>>()

        panel.contentChanged += observer
        panel.content         = content
        panel.content         = null

        expect(false) {
            content in panel.children_
        }

        verifyOrder {
            observer(panel, null,    content)
            observer(panel, content, null   )
        }
    }

    @Test @JsName("delegatesRenderToBehavior")
    fun `delegates render to behavior`() {
        val behavior = behavior()
        val panel    = panel(behavior)
        val canvas   = mockk<Canvas>()

        panel.render(canvas)

        verify(exactly = 1) { behavior.render(panel, canvas) }

        panel.behavior = null

        panel.render(canvas)

        verify { canvas wasNot Called }
    }

    @Test @JsName("delegatesContainsPointToBehavior")
    fun `delegates contains point to behavior`() {
        val behavior = behavior()
        val panel    = panel(behavior).apply { size = Size(100, 100) }
        val point    = Point(4, 78)

        panel.contains(point)

        verify(exactly = 1) { behavior.contains(panel, point) }
    }

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

            verify { behavior.scrollTo(panel, it) }
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
            val panel = panel(behavior).apply {
                scrollTo(start)

                expect(start + point) {
                    scrollBy(point)
                    scroll
                }
            }

            verify { behavior.scrollTo(panel, start + point) }
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
            val panel = panel(behavior).apply {
                scrollTo(start)

                expect(result) {
                    scrollToVisible(target)
                    scroll
                }
            }

            verify { behavior.scrollTo(panel, result) }
        }
    }

    @Test @JsName("scrollToVisibleRect")
    fun `scroll to visible rect`() {
        val behavior = behavior()
        val start    = Point(5.0, 5.0)

        mapOf(
                Rectangle(90, 90,  10,  10) to Point( 90, 90),
                Rectangle( 0,  0, 100, 100) to Point( 90, 90)
        ).forEach { (target, result) ->
            val panel = panel(behavior).apply {
                scrollTo(start)

                expect(result) {
                    scrollToVisible(target)
                    scroll
                }
            }

            verify { behavior.scrollTo(panel, result) }
        }
    }

    @Test @JsName("scrollHorizontallyToVisibleRect")
    fun `scroll horizontally to visible rect`() {
        val behavior = behavior()
        val start    = Point(5.0, 5.0)

        mapOf(
                90.0 .. 100.0 to Point(90, 5),
                 0.0 .. 100.0 to Point(90, 5)
        ).forEach { (target, result) ->
            val panel = panel(behavior).apply {
                scrollTo(start)

                expect(result) {
                    scrollHorizontallyToVisible(target)
                    scroll
                }
            }

            verify { behavior.scrollTo(panel, result) }
        }
    }

    @Test @JsName("scrollVerticallyToVisibleRect")
    fun `scroll vertically to visible rect`() {
        val behavior = behavior()
        val start    = Point(5.0, 5.0)

        mapOf(
                90.0 .. 100.0 to Point(5, 90),
                 0.0 .. 100.0 to Point(5, 90)
        ).forEach { (target, result) ->
            val panel = panel(behavior).apply {
                scrollTo(start)

                expect(result) {
                    scrollVerticallyToVisible(target)
                    scroll
                }
            }

            verify { behavior.scrollTo(panel, result) }
        }
    }

    @Test @JsName("widthConstraintsWork")
    fun `width constraints work`() {
        val content = spyk(container { size = Size(100, 100) })
        val panel   = ScrollPanel(content).apply {
            contentWidthConstraints = { parent.width.writable.readOnly }
        }

        panel.width = 300.0
        panel.doLayout_()

        expect(300.0) { content.width }
    }

    @Test @JsName("heightConstraintsWork")
    fun `height constraints work`() {
        val content = spyk(container { size = Size(100, 100) })
        val panel   = ScrollPanel(content).apply {
            contentHeightConstraints = { parent.height.writable.readOnly / 2 }
        }

        panel.height = 650.0
        panel.doLayout_()

        expect(325.0) { content.height }
    }

    @Test @JsName("idealSizeTracksContent")
    fun `ideal size tracks content`() {
        val content = object: View() {}
        val panel   = ScrollPanel(content)

        expect(null) { panel.idealSize }

        content.idealSize = Size(435, 30)

        expect(content.idealSize) { panel.idealSize }

        content.idealSize = null

        expect(null) { panel.idealSize }
    }

    @Test @JsName("obeysBehaviorOnScroll")
    fun `obeys behavior on-scroll`() {
        val onScroll = CapturingSlot<(Point) -> Unit>()

        val behavior = mockk<ScrollPanelBehavior>().apply {
            every { this@apply.onScroll = capture(onScroll) } just Runs
        }

        val panel = panel(behavior)
        val point = Point(30, 50)

        onScroll.captured(point)

        expect(point) { panel.scroll }
    }

    @Test @JsName("disconnectsOldBehaviorOnScroll")
    fun `disconnects old behavior on-scroll`() {
        val behavior = object: ScrollPanelBehavior {
            override var onScroll: ((Point) -> Unit)? = null
            override var scrollBarSizeChanged: ((ScrollPanelBehavior.ScrollBarType, Double) -> Unit)? = null

            override fun scrollTo(panel: ScrollPanel, point: Point) {}
        }

        val panel = panel(behavior)

        panel.behavior = null

        expect(null) { behavior.onScroll }
    }

    private fun panel(behavior: ScrollPanelBehavior = behavior()): ScrollPanel {
        val content = container { size = Size(100, 100) }

        return ScrollPanel(content).apply {
            this.behavior = behavior
            size          = Size(10, 10)
        }
    }

    private fun behavior(): ScrollPanelBehavior {
        val behavior = mockk<ScrollPanelBehavior>()

        val point    = CapturingSlot<Point>()
        val onScroll = CapturingSlot<(Point) -> Unit>()

        every { behavior.onScroll = capture(onScroll) } just Runs
        every { behavior.scrollTo(any(), capture(point)) } answers {
            onScroll.captured(point.captured)
        }

        return behavior
    }
}