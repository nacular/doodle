package io.nacular.doodle.core.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.nacular.doodle.core.ChildObserver
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult.Found
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.height
import io.nacular.doodle.core.view
import io.nacular.doodle.core.width
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Grab
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import javax.swing.JFrame
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect


/**
 * Created by Nicholas Eddy on 8/10/19.
 */
class DisplayImplTests {
    @Test fun `defaults valid`() {
        expect(true, "DisplayImpl::children.isEmpty()") { display().children.isEmpty() }

        mapOf(
            DisplayImpl::size               to Empty,
            DisplayImpl::width              to 0.0,
            DisplayImpl::height             to 0.0,
            DisplayImpl::cursor             to null,
            DisplayImpl::insets             to None,
            DisplayImpl::layout             to null,
            DisplayImpl::transform          to Identity
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test fun `notifies cursor change`() {
        val cursorObserver = mockk<PropertyObserver<Display, Cursor?>>()

        val display = display().apply {
            cursorChanged += cursorObserver
        }

        display.cursor = Grab

        verify { cursorObserver(display, null, Grab) }

        expect(Grab) { display.cursor!! }
    }

    @Test fun `notifies child added`() {
        val observer = mockk<ChildObserver<Display>>()

        val display = display().apply { childrenChanged += observer }

        val view = realView().apply { suggestPosition(x + 10.0, y + 12.0) }
        display += view

        verify (exactly = 1) {
            observer(display, Differences(listOf(Insert(view))))
        }
    }

    @Test fun `notifies child removed`() {
        val observer = mockk<ChildObserver<Display>>()

        val display = display()

        val view = realView().apply { suggestPosition(x + 10.0, y + 12.0) }
        display += view

        display.childrenChanged += observer

        display -= view

        verifyOrder {
            observer(display, Differences(listOf(Delete(view))))
        }
    }

    @Test fun `notifies child moved`() {
        val observer = mockk<ChildObserver<Display>>()

        val display = display()

        val view1 = realView().apply { suggestPosition(x + 10.0, y + 12.0) }
        val view2 = realView().apply { suggestPosition(x + 10.0, y + 12.0) }
        val view3 = realView().apply { suggestPosition(x + 10.0, y + 12.0) }

        display += listOf(view1, view2, view3)

        display.childrenChanged += observer

        display.children.move(view2, 0)

        verifyOrder {
            observer(display, Differences(listOf(Insert(view2), Equal(view1), Delete(view2), Equal(view3))))
        }
    }

    @Test fun `child at (no layout) works`() {
        val display = display()
        val child0  = mockView("child0", Rectangle(10, 12, 10, 10))
        val child1  = mockView("child1", Rectangle(10, 12, 10, 10))
        val child2  = mockView("child2", Rectangle(20, 12, 10, 10))
        val child3  = mockView("child3", Rectangle(10, 23,  0, 10))

        display += child0
        display += child1
        display += child2
        display += child3

        expect(child1) { display.child(at = Point(11.0, 13.0)) }
        expect(child2) { display.child(at = Point(20.0, 12.0)) }
        expect(null  ) { display.child(at = child3.position  ) }

        child1.visible = false

        expect(child0) { display.child(at = Point(11.0, 13.0)) }
    }

    @Test fun `child at works`() {
        val at            = Point(11.0, 13.0)
        val child         = realView()
        val positionables = slot<Sequence<Positionable>>()

        val layout = mockk<Layout>().apply {
            every { item(capture(positionables), at = at) } answers {
                Found(positionables.captured.first())
            }
        }

        display().apply {
            this += child

            this.layout = layout

            expect(child) { child(at) }

            every { layout.item(any(), at = at) } returns Ignored

            expect(null) { child(at) }

            verify(exactly = 2) { layout.item(any(), at) }
        }
    }

    @Test fun `is-ancestor works`() {
        val display = display()
        val parent  = container {}
        val child   = view {}

        expect(false) { display ancestorOf mockk() }
        expect(false) { display ancestorOf child   }

        display.children += parent
        parent.children  += child

        expect(true) { display ancestorOf parent }
        expect(true) { display ancestorOf child  }
    }

    @Test fun `layout works`() {
        val layout = mockk<Layout>()

        display().apply {
            relayout() // should no-op

            this.layout = layout

            verify (exactly= 1) { layout.layout(any(), any(), any(), any()) }

            relayout()

            verify (exactly= 2) { layout.layout(any(), any(), any(), any()) }
        }
    }

    @Test fun `plus equal child multiple times works`() {
        val display = display()
        val child0  = realView()
        val child1  = realView()
        val child2  = realView()

        display += child0
        display += child1
        display += child2
        display += child0

        expect(listOf(child1, child2, child0)) { display.children }
    }

    @Test fun `repeated child add works`() {
        val display = display()
        val child0  = realView()
        val child1  = realView()
        val child2  = realView()

        display.children.addAll(listOf(
            child0,
            child1,
            child2,
            child0
        ))

        expect(listOf(child1, child2, child0)) { display.children }
    }

    @Test fun `+= view works`() {
        val child   = mockk<Container>()
        val display = display()

        display += child

        expect(1    ) { display.children.size    }
        expect(child) { display.children.first() }
    }

    @Test fun `-= view works`() {
        val child   = mockk<Container>()
        val display = display()

        display += child
        display -= child

        expect(true) { display.children.isEmpty() }
    }

    @Test fun `+= collection works`() {
        val child1  = mockk<Container>()
        val child2  = mockk<View>     ()
        val display = display()

        display += listOf(child1, child2)

        expect(2     ) { display.children.size }
        expect(child1) { display.children[0]   }
        expect(child2) { display.children[1]   }
    }

    @Test fun `-= collection works`() {
        val child1  = mockk<Container>()
        val child2  = mockk<View>     ()
        val child3  = mockk<View>     ()
        val display = display()

        display += listOf(child1, child2, child3)

        display -= listOf(child1, child3)

        expect(1     ) { display.children.size    }
        expect(child2) { display.children.first() }
    }

    @Test fun `fill works`() {
        val renderDelegate = slot<SkikoRenderDelegate>()
        val skiaLayer = mockk<SkiaLayer> {
            every { this@mockk.renderDelegate = capture(renderDelegate) } answers {
                renderDelegate.captured
            }
        }
        val display   = display(skiaLayer = skiaLayer)
        val paint     = mockk<Paint>()

        verify(exactly = 0) {
            skiaLayer.needRedraw()
        }

        display.fill(paint)

        verify(exactly = 1) {
            skiaLayer.needRedraw()
        }

        val canvas = mockk<Canvas>()

        renderDelegate.captured.onRender(canvas, 400, 500, 1000L)

        verify(exactly = 1) {
            canvas.drawRect(any(), any())
        }
    }

    @Test fun `renders correctly`() {
        val renderDelegate = slot<SkikoRenderDelegate>()
        val skiaLayer      = mockk<SkiaLayer> {
            every { this@mockk.renderDelegate = capture(renderDelegate) } answers {
                renderDelegate.captured
            }
        }

        val child = mockk<View>()
        val popup = mockk<View>()

        val surfaces  = listOf(child, popup).associateWith { mockk<RealGraphicsSurface>() }
        val device    = mockk<GraphicsDevice<RealGraphicsSurface>>().apply {
            val view = slot<View>()

            every { this@apply[capture(view)] } answers {
                surfaces[view.captured]!!
            }
        }

        val display = display(device = device, skiaLayer = skiaLayer)

        display += child
        display.showPopup(popup)

        val canvas = mockk<Canvas>()

        renderDelegate.captured.onRender(canvas, 400, 500, 1000L)

        verify(exactly = 1) {
            surfaces.values.forEach {
                it.onRender(canvas)
            }
        }
    }

    private fun realView(): View = view { suggestBounds(Rectangle(size = Size(10.0, 10.0))) }

    private fun mockView(name: String? = null, bounds: Rectangle = Rectangle(Size(10))) = mockk<View>(name) {
        val point      = slot<Point>()
        var visible    = true
        val visibleSet = slot<Boolean>()

        every { zOrder                                   } returns 0
        every { this@mockk.visible                       } answers { visible }
        every { this@mockk.bounds                        } returns bounds
        every { contains(capture(point))                 } answers { point.captured in bounds }
        every { this@mockk.visible = capture(visibleSet) } answers { visible = visibleSet.captured }
    }

    private fun display(
        appScope      : CoroutineScope                      = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        uiDispatcher  : CoroutineContext                    = EmptyCoroutineContext,
        targetWindow  : JFrame                              = targetWindow(),
        defaultFont   : Font                                = mockk(),
        fontCollection: FontCollection                      = mockk(),
        device        : GraphicsDevice<RealGraphicsSurface> = mockk(),
        skiaLayer     : SkiaLayer                           = mockk(),
    ) = DisplayImpl(appScope, uiDispatcher, defaultFont, fontCollection, device, targetWindow) { skiaLayer }

    private fun targetWindow() = mockk<JFrame>().apply { every { contentPane } returns mockk<java.awt.Container>() }

    private fun <T> validateDefault(p: KProperty1<DisplayImpl, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(display()) }
    }
}