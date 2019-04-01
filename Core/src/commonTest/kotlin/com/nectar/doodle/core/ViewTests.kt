@file:Suppress("FunctionName")

package com.nectar.doodle.core

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Rectangle.Companion.Empty
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets.Companion.None
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.Cursor.Companion.Crosshair
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Drag
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.utils.PropertyObserver
import com.nectar.doodle.utils.PropertyObservers
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 2/23/18.
 */
class ViewTests {

    @Test @JsName("defaults")
    fun `defaults valid`() {
        expect("", "View::toolTipText") { object: View() {}.toolTipText }

        mapOf(
                View::x                   to 0.0,
                View::y                   to 0.0,
                View::font                to null,
                View::size                to Size.Empty,
                View::width               to 0.0,
                View::parent              to null,
                View::height              to 0.0,
                View::bounds              to Empty,
                View::cursor              to null,
                View::zOrder              to 0,
                View::enabled             to true,
                View::visible             to true,
                View::insets_             to None,
                View::layout_             to null,
                View::position            to Origin,
                View::hasFocus            to false,
                View::focusable           to true,
                View::idealSize           to null,
                View::displayRect         to Empty,
                View::minimumSize         to Size.Empty,
                View::monitorsMouse       to false,
                View::foregroundColor     to null,
                View::backgroundColor     to null,
                View::monitorsKeyboard    to false,
                View::monitorsMouseScroll to false,
                View::monitorsMouseMotion to false,
                View::monitorsDisplayRect to false
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("settersWork")
    fun `setters work`() {
        object: View() {}.also {
            val value = "foo"
            it.toolTipText = value

            expect(value, "toolTipText set to $value") { it.toolTipText }
        }

        validateSetter(View::x,                   -5.0                            )
        validateSetter(View::y,                   6.0                             )
        validateSetter(View::font,                null                            )
        validateSetter(View::size,                Size.Empty                      )
        validateSetter(View::width,               99.0                            )
        validateSetter(View::zOrder,              56                              )
        validateSetter(View::height,              45.0                            )
        validateSetter(View::bounds,              Rectangle(4.5, -3.0, 2.0, 45.5) )
        validateSetter(View::cursor,              Crosshair                )
        validateSetter(View::enabled,             false                           )
        validateSetter(View::visible,             false                           )
        validateSetter(View::position,            Origin                          )
        validateSetter(View::focusable,           false                           )
        validateSetter(View::idealSize,           Size(20.0, 37.6)                )
        validateSetter(View::minimumSize,         Size.Empty                      )
        validateSetter(View::monitorsMouse,       false                           )
        validateSetter(View::foregroundColor,     red                             )
        validateSetter(View::backgroundColor,     green                           )
        validateSetter(View::monitorsKeyboard,    false                           )
        validateSetter(View::monitorsMouseScroll, false                           )
        validateSetter(View::monitorsMouseMotion, false                           )
        validateSetter(View::monitorsDisplayRect, false                           )
    }

    @Test @JsName("rerenderWorks")
    fun `rerender work`() {
        val renderManager = mockk<RenderManager>(relaxed = true)
        val view         = object: View() {}

        view.addedToDisplay(renderManager)

        view.rerender()

        verify(exactly = 1) { renderManager.render(view) }
    }

    @Test @JsName("rerenderNowWorks")
    fun `rerenderNow work`() {
        val renderManager = mockk<RenderManager>(relaxed = true)
        val view         = object: View() {}

        view.addedToDisplay(renderManager)

        view.rerenderNow()

        verify(exactly = 1) { renderManager.renderNow(view) }
    }

    @Test @JsName("parentChangeWorks")
    fun `parent change works`() {
        val view   = object: View() {}
        val parent = Box()

        val observer = mockk<PropertyObserver<View, View?>>(relaxed = true)

        view.parentChange += observer

        parent.children += view

        verify(exactly = 1) { observer(view, null, parent) }

        parent.children -= view

        verify(exactly = 1) { observer(view, parent, null) }
    }

    @Test @JsName("displayChangeWorks")
    fun `display change works`() {
        val view = object: View() {}

        val observer = mockk<PropertyObserver<View, Boolean>>(relaxed = true)

        view.displayChange += observer

        view.addedToDisplay(mockk())

        verify(exactly = 1) { observer(view, false, true) }

        view.removedFromDisplay_()

        verify(exactly = 1) { observer(view, true, false) }
    }

    @Test @JsName("changeEventsWork")
    fun `change events work`() {
        listOf(
            View::enabled             to View::enabledChanged,
            View::visible             to View::visibilityChanged,
            View::focusable           to View::focusabilityChanged,
            View::focusable           to View::focusabilityChanged,
            View::monitorsDisplayRect to View::displayRectHandlingChanged
        ).forEach {
            validateChanged(it.first, it.second)
        }
    }

    @Test @JsName("mouseEventsWorks")
    fun `mouse events works`() = validateMouseChanged(mockk<MouseEvent>().apply { every { type } returns Enter }) { listener, event ->
        verify(exactly = 1) { listener.mouseEntered(event) }
    }

    @Test @JsName("mouseExitWorks")
    fun `mouse exit works`() = validateMouseChanged(mockk<MouseEvent>().apply { every { type } returns Exit }) { listener, event ->
        verify(exactly = 1) { listener.mouseExited(event) }
    }

    @Test @JsName("mousePressedWorks")
    fun `mouse pressed works`() = validateMouseChanged(mockk<MouseEvent>().apply { every { type } returns Down }) { listener, event ->
        verify(exactly = 1) { listener.mousePressed(event) }
    }

    @Test @JsName("mouseReleasedWorks")
    fun `mouse released works`() = validateMouseChanged(mockk<MouseEvent>().apply { every { type } returns Up }) { listener, event ->
        verify(exactly = 1) { listener.mouseReleased(event) }
    }

    @Test @JsName("mouseMoveWorks")
    fun `mouse move works`() = validateMouseMotionChanged(mockk<MouseEvent>().apply { every { type } returns Move }) { listener, event ->
        verify(exactly = 1) { listener.mouseMoved(event) }
    }

    @Test @JsName("mouseDragWorks")
    fun `mouse drag works`() = validateMouseMotionChanged(mockk<MouseEvent>().apply { every { type } returns Drag }) { listener, event ->
        verify(exactly = 1) { listener.mouseDragged(event) }
    }

    @Test @JsName("focusGainedWorks")
    fun `focus gained works`() = validateFocusChanged(true) { view, observer ->
        verify(exactly = 1) { observer(view, false, true) }
    }

    @Test @JsName("focusLostWorks")
    fun `focus lost works`() = validateFocusChanged(false) { view, observer ->
        verify(exactly = 1) { observer(view, true, false) }
    }

    @Test @JsName("boundsChangedWorks")
    fun `bounds changed works`() {
        val view    = object: View() {}
        val observer = mockk<PropertyObserver<View, Rectangle>>(relaxed = true)
        val new      = Rectangle(5.6, 3.7, 900.0, 1.2)
        val old      = view.bounds

        view.boundsChanged += observer
        view.bounds         = new

        verify(exactly = 1) { observer(view, old, new) }

        view.x = 67.0

        verify(exactly = 1) { observer(view, new, new.at(x = 67.0)) }
    }

    @Test @JsName("cursorChangedWorks")
    fun `cursor changed works`() {
        val view    = object: View() {}
        val observer = mockk<PropertyObserver<View, Cursor?>>(relaxed = true)
        val new      = Crosshair
        val old      = view.cursor

        view.cursorChanged += observer
        view.cursor         = new
        view.cursor         = new

        verify(exactly = 1) { observer(view, old, new) }
    }

    @Test @JsName("zOrderChangeWorks")
    fun `z-order change works`() {
        val view    = object: View() {}
        val observer = mockk<PropertyObserver<View, Int>>(relaxed = true)
        val new      = 35
        val old      = view.zOrder

        view.zOrderChanged += observer
        view.zOrder         = new
        view.zOrder         = new

        verify(exactly = 1) { observer(view, old, new) }
    }

    @Test @JsName("containsPointWorks")
    fun `contains point`() {
        val view = object: View() {}
        val bounds = Rectangle(10.0, 10.0, 25.0, 25.0)

        expect(false, "$view contains ${bounds.position}") { bounds.position in view }

        view.bounds = bounds

        expect(true, "$view contains ${bounds.position}") { bounds.position in view }

        view.size = Size.Empty

        expect(false, "$view contains ${bounds.position}") { bounds.position in view }
    }

    @Test @JsName("toolTipTextWorks")
    fun `tool-top text works`() {
        val view = object: View() {}
        val event = mockk<MouseEvent>(relaxed = true)

        expect("", "${view.toolTipText} == \"\"") { view.toolTipText(event) }

        view.toolTipText = "foo"

        expect("foo", "${view.toolTipText} == \"\"") { view.toolTipText(event) }
    }

    @Test @JsName("isAncestorWorks")
    fun `is-ancestor works`() {
        val root   = object: View() {}
        val parent = object: View() {}
        val child  = object: View() {}

        expect(false) { root ancestorOf_ root  }
        expect(false) { root ancestorOf_ child }

        root.children_   += parent
        parent.children_ += child

        expect(true) { root ancestorOf_ parent }
        expect(true) { root ancestorOf_ child  }
    }

    @Test @JsName("toAbsoluteWorks")
    fun `to absolute works`() {
        val root   = view()
        val parent = view().apply { x += 10.0; y += 12.0 }
        val child  = view().apply { x += 10.0; y += 12.0 }

        root.children_   += parent
        parent.children_ += child

        val point = Point(100.0, 56.0)

        expect(point + parent.position                 ) { parent.toAbsolute(point) }
        expect(point + parent.position + child.position) { child.toAbsolute (point) }
    }

    @Test @JsName("fromAbsoluteWorks")
    fun `from absolute works`() {
        val root   = view()
        val parent = view().apply { x += 10.0; y += 12.0 }
        val child  = view().apply { x += 10.0; y += 12.0 }

        root.children_   += parent
        parent.children_ += child

        val point = Point(100.0, 56.0)

        expect(point -  parent.position                  ) { parent.fromAbsolute(point) }
        expect(point - (parent.position + child.position)) { child.fromAbsolute (point) }

        expect(child.fromAbsolute(point)) { child.toLocal(point, root) }
    }

    @Test @JsName("toLocalWorks")
    fun `to local works`() {
        val root   = view()
        val parent = view().apply { x += 10.0; y += 12.0 }
        val child1 = view().apply { x += 10.0; y += 12.0 }
        val child2 = view().apply { x += 20.0; y += 12.0 }

        root.children_   += parent
        parent.children_ += child1
        parent.children_ += child2

        expect(Origin                           ) { parent.toLocal(parent.position,    root  ) }
        expect(Point(-45.0, 0.89)               ) { root.toLocal  (Point(-45.0, 0.89), root  ) }
        expect(child2.position - child1.position) { child1.toLocal(Origin,             child2) }
    }

    @Test @JsName("childAtWorks")
    fun `child at works`() {
        val root   = view()
        val child0 = view().apply { x += 10.0; y += 12.0 }
        val child1 = view().apply { x += 10.0; y += 12.0 }
        val child2 = view().apply { x += 20.0; y += 12.0 }
        val child3 = view().apply { x += 10.0; y += 23.0; width = 0.0 }

        root.children_ += child0
        root.children_ += child1
        root.children_ += child2
        root.children_ += child3

        expect(child1) { root.child_(Point(11.0, 13.0)) }
        expect(child2) { root.child_(Point(20.0, 12.0)) }
        expect(null  ) { root.child_(child3.position  ) }

        child1.visible = false

        expect(child0) { root.child_(Point(11.0, 13.0)) }
    }

    private fun validateFocusChanged(gained: Boolean, block: (View, PropertyObserver<View, Boolean>) -> Unit) {
        val view     = object: View() {}
        val observer = mockk<PropertyObserver<View, Boolean>>(relaxed = true)

        view.focusChanged += observer

        // Force the View to have focus if we are testing losing it
        if (!gained) {
            view.focusGained(null)
        }

        if (gained) {
            view.focusGained(null)
        } else {
            view.focusLost(null)
        }

        block(view, observer)
    }

    private fun validateMouseChanged(event: MouseEvent, block: (MouseListener, MouseEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<MouseListener>(relaxed = true)

        view.mouseChanged += listener

        view.handleMouseEvent_(event)

        block(listener, event)
    }

    private fun validateMouseMotionChanged(event: MouseEvent, block: (MouseMotionListener, MouseEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<MouseMotionListener>(relaxed = true)

        view.mouseMotionChanged += listener

        view.handleMouseMotionEvent_(event)

        block(listener, event)
    }

    private fun validateChanged(property: KMutableProperty1<View, Boolean>, changed: KProperty1<View, PropertyObservers<View, Boolean>>) {
        val view     = object: View() {}
        val old      = property.get(view)
        val observer = mockk<PropertyObserver<View, Boolean>>(relaxed = true)

        changed.get(view).plusAssign(observer)

        property.set(view, !property.get(view))

        verify(exactly = 1) { observer(view, old, property.get(view)) }
    }

    private fun <T> validateDefault(p: KProperty1<View, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(object: View() {}) }
    }

    private fun <T> validateSetter(p: KMutableProperty1<View, T>, value: T) {
        object: View() {}.also {
            p.set(it, value)

            expect(value, "$p set to $value") { p.get(it) }
        }
    }

    private fun view(): View = object: View() {}.apply { bounds = Rectangle(size = Size(10.0, 10.0)) }
}