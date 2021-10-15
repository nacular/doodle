package io.nacular.doodle.core.impl

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.nacular.doodle.HTMLElement
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult.Found
import io.nacular.doodle.core.LookupResult.Ignored
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.height
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.view
import io.nacular.doodle.core.width
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.utils.PropertyObserver
import JsName
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect


/**
 * Created by Nicholas Eddy on 8/10/19.
 */

@Suppress("FunctionName")
class DisplayImplTests {
    @Test @JsName("defaultsValid") fun `defaults valid`() {
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

    @Test @JsName("registersOnResize") fun `registers onresize`() {
        val rootElement = mockk<HTMLElement>()

        display(rootElement = rootElement)

        verify(exactly = 1) { rootElement.onresize = any() }
    }

    @Test @JsName("hasInitialWindowSize") fun `has initial window size`() {
        val rootElement = mockk<HTMLElement>().apply {
            every { offsetWidth  } returns 100
            every { offsetHeight } returns 150
        }

        expect(Size(100, 150)) { display(rootElement = rootElement).size }
    }

    @Test @JsName("handlesWindowResize") fun `handles window resize`() {
        var slot = slot<(Event) -> Unit>()

        val rootElement = mockk<HTMLElement>().apply {
            every { onresize = captureLambda() } answers {
                slot = lambda()
            }
        }

        val sizeObserver = mockk<PropertyObserver<Display, Size>>()

        val display = display(rootElement = rootElement).apply {
            sizeChanged += sizeObserver
        }

        val newSize = Size(100, 150)

        rootElement.apply {
            every { offsetWidth  } returns newSize.width.toInt ()
            every { offsetHeight } returns newSize.height.toInt()
        }

        slot.captured(mockk())

        verify { sizeObserver(display, Empty, newSize) }

        expect(newSize) { display.size }
    }

    @Test @JsName("notifiesCursorChange") fun `notifies cursor change`() {
        val cursorObserver = mockk<PropertyObserver<Display, Cursor?>>()

        val display = display().apply {
            cursorChanged += cursorObserver
        }

        display.cursor = Cursor.Grab

        verify { cursorObserver(display, null, Cursor.Grab) }

        expect(Cursor.Grab) { display.cursor!! }
    }

    @Test @JsName("childAtNoLayoutWorks") fun `child at (no layout) works`() {
        val display = display()
        val child0  = view().apply { x += 10.0; y += 12.0 }
        val child1  = view().apply { x += 10.0; y += 12.0 }
        val child2  = view().apply { x += 20.0; y += 12.0 }
        val child3  = view().apply { x += 10.0; y += 23.0; width = 0.0 }

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

    @Test @JsName("childAtWorks") fun `child at works`() {
        val at     = Point(11.0, 13.0)
        val result = mockk<View>()
        val layout = mockk<Layout>().apply {
            every { child(any(), at = at) } returns Found(result)
        }

        display().apply {
            this.layout = layout

            expect(result) { child(at) }

            every { layout.child(any(), at = at) } returns Ignored

            expect(null) { child(at) }

            verify(exactly = 2) { layout.child(any(), at) }
        }
    }

    @Test @JsName("isAncestorWorks") fun `is-ancestor works`() {
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

    @Test @JsName("layoutWorks") fun `layout works`() {
        val layout = mockk<Layout>()

        display().apply {
            relayout() // should no-op

            this.layout = layout

            verify (exactly= 1) { layout.layout(any()) }

            relayout()

            verify (exactly= 2) { layout.layout(any()) }
        }
    }

    private fun view(): View = object: View() {}.apply { bounds = Rectangle(size = Size(10.0, 10.0)) }

    private fun display(htmlFactory  : HtmlFactory   = mockk(),
                        canvasFactory: CanvasFactory = mockk(),
                        rootElement  : HTMLElement   = mockk()) = DisplayImpl(htmlFactory, canvasFactory, rootElement)

    private fun <T> validateDefault(p: KProperty1<DisplayImpl, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(display()) }
    }
}