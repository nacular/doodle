@file:Suppress("FunctionName")

package io.nacular.doodle.core

import io.nacular.doodle.JsName
import io.nacular.doodle.drawing.Color.Companion.Green
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyState.Type
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets.Companion.None
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Crosshair
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.assertFailsWith
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
                View::displayed           to false,
                View::focusable           to true,
                View::idealSize           to null,
                View::displayRect         to Empty,
                View::minimumSize         to Size.Empty,
                View::acceptsThemes       to true,
                View::foregroundColor     to null,
                View::backgroundColor     to null,
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

        validateSetter(View::x,                   -5.0                           )
        validateSetter(View::y,                   6.0                            )
        validateSetter(View::font,                null                           )
        validateSetter(View::size,                Size.Empty                     )
        validateSetter(View::width,               99.0                           )
        validateSetter(View::zOrder,              56                             )
        validateSetter(View::height,              45.0                           )
        validateSetter(View::bounds,              Rectangle(4.5, -3.0, 2.0, 45.5))
        validateSetter(View::cursor,              Crosshair                      )
        validateSetter(View::enabled,             false                          )
        validateSetter(View::visible,             false                          )
        validateSetter(View::position,            Origin                         )
        validateSetter(View::focusable,           false                          )
        validateSetter(View::idealSize,           Size(20.0, 37.6)               )
        validateSetter(View::minimumSize,         Size.Empty                     )
        validateSetter(View::foregroundColor,     Red                            )
        validateSetter(View::backgroundColor,     Green                          )
        validateSetter(View::monitorsDisplayRect, false                          )
    }

    @Test @JsName("rerenderWorks")
    fun `rerender work`() {
        val display       = mockk<Display>()
        val renderManager = mockk<RenderManager>()
        val view          = object: View() {}

        view.addedToDisplay(display, renderManager, null)

        view.rerender()

        verify(exactly = 1) { renderManager.render(view) }
    }

    @Test @JsName("rerenderNowWorks")
    fun `rerenderNow work`() {
        val display       = mockk<Display>()
        val renderManager = mockk<RenderManager>()
        val view          = object: View() {}

        view.addedToDisplay(display, renderManager, null)

        view.rerenderNow()

        verify(exactly = 1) { renderManager.renderNow(view) }
    }

    @Test @JsName("parentChangeWorks")
    fun `parent change works`() {
        val view   = object: View() {}
        val parent = Box()

        val observer = mockk<PropertyObserver<View, View?>>()

        view.parentChange += observer

        parent.children += view

        verify(exactly = 1) { observer(view, null, parent) }

        parent.children -= view

        verify(exactly = 1) { observer(view, parent, null) }
    }

    @Test @JsName("displayChangeWorks")
    fun `display change works`() {
        val view = object: View() {}

        val observer = mockk<PropertyObserver<View, Boolean>>()

        view.displayChange += observer

        view.addedToDisplay(mockk(), mockk(), null)

        verify(exactly = 1) { observer(view, false, true) }

        view.removedFromDisplay_()

        verify(exactly = 1) { observer(view, true, false) }
    }

    private class SubView: View() {
        public override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) { super.handleDisplayRectEvent(old, new) }
        public override fun handlePointerEvent(event: PointerEvent) { super.handlePointerEvent(event) }
        public override fun handleKeyEvent(event: KeyEvent) { super.handleKeyEvent(event) }
        public override fun handlePointerMotionEvent(event: PointerEvent) { super.handlePointerMotionEvent(event) }
    }

    @Test @JsName("forwardsDisplayRectToSubclass")
    fun `forwards display-rect to subclass`() {
        val view = spyk(SubView())
        val old  = Rectangle(100)
        val new  = Rectangle( 30)

        view.handleDisplayRectEvent_(old, new)

        verify(exactly = 1) { view.handleDisplayRectEvent(old, new) }
    }

    @Test @JsName("forwardsPointerEventToSubclass")
    fun `forwards pointer event to subclass`() {
        val view  = spyk(SubView())
        val event = mockk<PointerEvent>()

        view.handlePointerEvent_(event)

        verify(exactly = 1) { view.handlePointerEvent(event) }
    }

    @Test @JsName("forwardsKeyEventToSubclass")
    fun `forwards key event to subclass`() {
        val view  = spyk(SubView())
        val event = mockk<KeyEvent>()

        view.handleKeyEvent_(event)

        verify(exactly = 1) { view.handleKeyEvent(event) }
    }

    @Test @JsName("forwardsPointerMotionEventToSubclass")
    fun `forwards pointer motion event to subclass`() {
        val view  = spyk(SubView())
        val event = mockk<PointerEvent>()

        view.handlePointerMotionEvent_(event)

        verify(exactly = 1) { view.handlePointerMotionEvent(event) }
    }

    @Test @JsName("centerWorks")
    fun `center works`() {
        val view = view()

        listOf(
            Rectangle(        100, 100),
            Rectangle(12, 38,  10, 100)
        ).forEach {
            view.bounds = it
            expect(view.center) { it.center }
        }
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

    @Test @JsName("styleChangeEventsWork")
    fun `style change events work`() {
        validateStyleChanged(View::font,            mockk())
        validateStyleChanged(View::foregroundColor, mockk())
        validateStyleChanged(View::backgroundColor, mockk())
    }

    @Test @JsName("keyDownEventsWorks")
    fun `key down events works`() = validateKeyChanged(mockk<KeyEvent>().apply { every { type } returns Type.Down }) { listener, event ->
        verify(exactly = 1) { listener.keyPressed(event) }
    }

    @Test @JsName("keyUpEventsWorks")
    fun `key up events works`() = validateKeyChanged(mockk<KeyEvent>().apply { every { type } returns Type.Up }) { listener, event ->
        verify(exactly = 1) { listener.keyReleased(event) }
    }

    @Test @JsName("pointerEventsWorks")
    fun `pointer events works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Enter }) { listener, event ->
        verify(exactly = 1) { listener.entered(event) }
    }

    @Test @JsName("pointerExitWorks")
    fun `pointer exit works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Exit }) { listener, event ->
        verify(exactly = 1) { listener.exited(event) }
    }

    @Test @JsName("pointerPressedWorks")
    fun `pointer pressed works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Down }) { listener, event ->
        verify(exactly = 1) { listener.pressed(event) }
    }

    @Test @JsName("pointerReleasedWorks")
    fun `pointer released works`() = validatePointerChanged(mockk<PointerEvent>().apply { every { type } returns Up }) { listener, event ->
        verify(exactly = 1) { listener.released(event) }
    }

    @Test @JsName("pointerMoveWorks")
    fun `pointer move works`() = validatePointerMotionChanged(mockk<PointerEvent>().apply { every { type } returns Move }) { listener, event ->
        verify(exactly = 1) { listener.moved(event) }
    }

    @Test @JsName("pointerDragWorks")
    fun `pointer drag works`() = validatePointerMotionChanged(mockk<PointerEvent>().apply { every { type } returns Drag }) { listener, event ->
        verify(exactly = 1) { listener.dragged(event) }
    }

    @Test @JsName("filterPointerReleasedWorks")
    fun `filter pointer released works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Up }) { listener, event ->
        verify(exactly = 1) { listener.released(event) }
    }

    @Test @JsName("filterPointerMoveWorks")
    fun `filter pointer move works`() = validatePointerMotionFilter(mockk<PointerEvent>().apply { every { type } returns Move }) { listener, event ->
        verify(exactly = 1) { listener.moved(event) }
    }

    @Test @JsName("filterPointerEventsWorks")
    fun `filter pointer events works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Enter }) { listener, event ->
        verify(exactly = 1) { listener.entered(event) }
    }

    @Test @JsName("filterPointerExitWorks")
    fun `filter pointer exit works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Exit }) { listener, event ->
        verify(exactly = 1) { listener.exited(event) }
    }

    @Test @JsName("filterPointerPressedWorks")
    fun `filter pointer pressed works`() = validatePointerFilter(mockk<PointerEvent>().apply { every { type } returns Down }) { listener, event ->
        verify(exactly = 1) { listener.pressed(event) }
    }

    @Test @JsName("filterPointerDragWorks")
    fun `filter pointer drag works`() = validatePointerMotionFilter(mockk<PointerEvent>().apply { every { type } returns Drag }) { listener, event ->
        verify(exactly = 1) { listener.dragged(event) }
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
        val observer = mockk<PropertyObserver<View, Rectangle>>()
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
        val observer = mockk<PropertyObserver<View, Cursor?>>()
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
        val observer = mockk<PropertyObserver<View, Int>>()
        val new      = 35
        val old      = view.zOrder

        view.zOrderChanged += observer
        view.zOrder         = new
        view.zOrder         = new

        verify(exactly = 1) { observer(view, old, new) }
    }

    @Test @JsName("containsPointWorks")
    fun `contains point works`() {
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
        val event = mockk<PointerEvent>()

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

    @Test @JsName("cannotBeParentToSelf")
    fun `cannot be parent to self`() {
        val view = object: View() {
            public override val children: ObservableList<View>
                get() = super.children
        }

        assertFailsWith<IllegalArgumentException> { view.children += view }
    }

    @Test @JsName("cannotAddAncestorToChildren")
    fun `cannot add ancestor to children`() {
        val grandParent = object: View() {
            public override val children get() = super.children
        }

        val parent = object: View() {
            public override val children get() = super.children
        }

        val child = object: View() {
            public override val children get() = super.children
        }

        grandParent.children += parent
        parent.children      += child

        assertFailsWith<IllegalArgumentException> { child.children += grandParent }
    }

    private fun validateFocusChanged(gained: Boolean, block: (View, PropertyObserver<View, Boolean>) -> Unit) {
        val view     = object: View() {}
        val observer = mockk<PropertyObserver<View, Boolean>>()

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

    private fun validateKeyChanged(event: KeyEvent, block: (KeyListener, KeyEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<KeyListener>()

        view.keyChanged += listener

        view.handleKeyEvent_(event)

        block(listener, event)
    }

    private fun validatePointerChanged(event: PointerEvent, block: (PointerListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerListener>()

        view.pointerChanged += listener

        view.handlePointerEvent_(event)

        block(listener, event)
    }

    private fun validatePointerFilter(event: PointerEvent, block: (PointerListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerListener>()

        view.pointerFilter += listener

        view.filterPointerEvent_(event)

        block(listener, event)
    }

    private fun validatePointerMotionChanged(event: PointerEvent, block: (PointerMotionListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerMotionListener>()

        view.pointerMotionChanged += listener

        view.handlePointerMotionEvent_(event)

        block(listener, event)
    }

    private fun validatePointerMotionFilter(event: PointerEvent, block: (PointerMotionListener, PointerEvent) -> Unit) {
        val view     = object: View() {}
        val listener = mockk<PointerMotionListener>()

        view.pointerMotionFilter += listener

        view.filterPointerMotionEvent_(event)

        block(listener, event)
    }

    private fun validateChanged(property: KMutableProperty1<View, Boolean>, changed: KProperty1<View, PropertyObservers<View, Boolean>>) {
        val view     = object: View() {}
        val old      = property.get(view)
        val observer = mockk<PropertyObserver<View, Boolean>>()

        changed.get(view).plusAssign(observer)

        property.set(view, !property.get(view))

        verify(exactly = 1) { observer(view, old, property.get(view)) }
    }

    private fun <T: Any?> validateStyleChanged(property: KMutableProperty1<View, T>, value: T) {
        val view     = object: View() {}
        val observer = mockk<ChangeObserver<View>>()

        view.styleChanged += observer

        property.set(view, value)

        verify(exactly = 1) { observer(view) }
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