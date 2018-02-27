package com.nectar.doodle.core

import com.nectar.doodle.JsName
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.FocusEvent
import com.nectar.doodle.event.FocusEvent.Type.Gained
import com.nectar.doodle.event.FocusEvent.Type.Lost
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
class GizmoTests {

    @Test @JsName("defaults")
    fun `defaults valid`() {
        expect("", "Gizmo::toolTipText") { object: Gizmo() {}.toolTipText }

        mapOf(
                Gizmo::x                   to 0.0,
                Gizmo::y                   to 0.0,
                Gizmo::font                to null,
                Gizmo::name                to "",
                Gizmo::size                to Size.Empty,
                Gizmo::width               to 0.0,
                Gizmo::parent              to null,
                Gizmo::height              to 0.0,
                Gizmo::bounds              to Empty,
                Gizmo::cursor              to null,
                Gizmo::enabled             to true,
                Gizmo::visible             to true,
                Gizmo::insets_             to None,
                Gizmo::layout_             to null,
                Gizmo::position            to Origin,
                Gizmo::hasFocus            to false,
                Gizmo::focusable           to true,
                Gizmo::idealSize           to null,
                Gizmo::displayRect         to Empty,
                Gizmo::minimumSize         to Size.Empty,
                Gizmo::monitorsMouse       to false,
                Gizmo::foregroundColor     to null,
                Gizmo::backgroundColor     to null,
                Gizmo::monitorsKeyboard    to true,
                Gizmo::monitorsMouseWheel  to true,
                Gizmo::monitorsMouseMotion to false,
                Gizmo::monitorsDisplayRect to false
        ).forEach { validateDefault(it.key, it.value) }
    }

    @Test @JsName("settersWork")
    fun `setters work`() {
        object: Gizmo() {}.also {
            val value = "foo"
            it.toolTipText = value

            expect(value, "toolTipText set to $value") { it.toolTipText }
        }

        validateSetter(Gizmo::x,                   -5.0                            )
        validateSetter(Gizmo::y,                   6.0                             )
        validateSetter(Gizmo::font,                null                            )
        validateSetter(Gizmo::name,                ""                              )
        validateSetter(Gizmo::size,                Size.Empty                      )
        validateSetter(Gizmo::width,               99.0                            )
        validateSetter(Gizmo::height,              45.0                            )
        validateSetter(Gizmo::bounds,              Rectangle(4.5, -3.0, 2.0, 45.5) )
        validateSetter(Gizmo::cursor,              Cursor.Crosshair                )
        validateSetter(Gizmo::enabled,             false                           )
        validateSetter(Gizmo::visible,             false                           )
        validateSetter(Gizmo::position,            Origin                          )
        validateSetter(Gizmo::focusable,           false                           )
        validateSetter(Gizmo::idealSize,           Size(20.0, 37.6)                )
        validateSetter(Gizmo::minimumSize,         Size.Empty                      )
        validateSetter(Gizmo::monitorsMouse,       false                           )
        validateSetter(Gizmo::foregroundColor,     red                             )
        validateSetter(Gizmo::backgroundColor,     green                           )
        validateSetter(Gizmo::monitorsKeyboard,    true                            )
        validateSetter(Gizmo::monitorsMouseWheel,  true                            )
        validateSetter(Gizmo::monitorsMouseMotion, false                           )
        validateSetter(Gizmo::monitorsDisplayRect, false                           )
    }

    @Test @JsName("rerenderWorks")
    fun `rerender work`() {
        val renderManager = mockk<RenderManager>(relaxed = true)
        val gizmo         = object: Gizmo() {}

        gizmo.addedToDisplay(renderManager)

        gizmo.rerender()

        verify(exactly = 1) { renderManager.render(gizmo) }
    }

    @Test @JsName("rerenderNowWorks")
    fun `rerenderNow work`() {
        val renderManager = mockk<RenderManager>(relaxed = true)
        val gizmo         = object: Gizmo() {}

        gizmo.addedToDisplay(renderManager)

        gizmo.rerenderNow()

        verify(exactly = 1) { renderManager.renderNow(gizmo) }
    }

    @Test @JsName("changeEventsWork")
    fun `change events work`() {
        listOf(
            Gizmo::enabled   to Gizmo::enabledChanged,
            Gizmo::visible   to Gizmo::visibilityChanged,
            Gizmo::focusable to Gizmo::focusableChanged
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
    fun `focus gained works`() = validateFocusChanged(mockk<FocusEvent>(relaxed = true).apply { every { type } returns Gained }) { gizmo, observer, _ ->
        verify(exactly = 1) { observer(gizmo, false, true) }
    }

    @Test @JsName("focusLostWorks")
    fun `focus lost works`() = validateFocusChanged(mockk<FocusEvent>(relaxed = true).apply { every { type } returns Lost }) { gizmo, observer, _ ->
        verify(exactly = 1) { observer(gizmo, true, false) }
    }

    @Test @JsName("boundsChangedWorks")
    fun `bounds changed works`() {
        val gizmo    = object: Gizmo() {}
        val observer = mockk<PropertyObserver<Gizmo, Rectangle>>(relaxed = true)
        val new      = Rectangle(5.6, 3.7, 900.0, 1.2)
        val old      = gizmo.bounds

        gizmo.boundsChanged += observer
        gizmo.bounds         = new

        verify(exactly = 1) { observer(gizmo, old, new) }

        gizmo.x = 67.0

        verify(exactly = 1) { observer(gizmo, new, new.at(x = 67.0)) }
    }

    @Test @JsName("cursorChangedWorks")
    fun `cursor changed works`() {
        val gizmo    = object: Gizmo() {}
        val observer = mockk<PropertyObserver<Gizmo, Cursor?>>(relaxed = true)
        val new      = Cursor.Crosshair
        val old      = gizmo.cursor

        gizmo.cursorChanged += observer
        gizmo.cursor         = new
        gizmo.cursor         = new

        verify(exactly = 1) { observer(gizmo, old, new) }
    }

    @Test @JsName("containsPointWorks")
    fun `contains point`() {
        val gizmo = object: Gizmo() {}
        val bounds = Rectangle(10.0, 10.0, 25.0, 25.0)

        expect(false, "$gizmo contains ${bounds.position}") { gizmo.contains(bounds.position) }

        gizmo.bounds = bounds

        expect(true, "$gizmo contains ${bounds.position}") { gizmo.contains(bounds.position) }

        gizmo.size = Size.Empty

        expect(false, "$gizmo contains ${bounds.position}") { gizmo.contains(bounds.position) }
    }

    @Test @JsName("toolTipTextWorks")
    fun `tool-top text works`() {
        val gizmo = object: Gizmo() {}
        val event = mockk<MouseEvent>(relaxed = true)

        expect("", "${gizmo.toolTipText} == \"\"") { gizmo.toolTipText(event) }

        gizmo.toolTipText = "foo"

        expect("foo", "${gizmo.toolTipText} == \"\"") { gizmo.toolTipText(event) }
    }

    @Test @JsName("isAncestorWorks")
    fun `is-ancestor works`() {
        val root   = object: Gizmo() {}
        val parent = object: Gizmo() {}
        val child  = object: Gizmo() {}

        expect(false) { root.isAncestor_(root ) }
        expect(false) { root.isAncestor_(child) }

        root.children_   += parent
        parent.children_ += child

        expect(true) { root.isAncestor_(parent) }
        expect(true) { root.isAncestor_(child ) }
    }

    @Test @JsName("toAbsoluteWorks")
    fun `to absolute works`() {
        val root   = gizmo()
        val parent = gizmo().apply { x += 10.0; y += 12.0 }
        val child  = gizmo().apply { x += 10.0; y += 12.0 }

        root.children_   += parent
        parent.children_ += child

        val point = Point(100.0, 56.0)

        expect(point + parent.position                 ) { parent.toAbsolute(point) }
        expect(point + parent.position + child.position) { child.toAbsolute (point) }
    }

    @Test @JsName("fromAbsoluteWorks")
    fun `from absolute works`() {
        val root   = gizmo()
        val parent = gizmo().apply { x += 10.0; y += 12.0 }
        val child  = gizmo().apply { x += 10.0; y += 12.0 }

        root.children_   += parent
        parent.children_ += child

        val point = Point(100.0, 56.0)

        expect(point -  parent.position                  ) { parent.fromAbsolute(point) }
        expect(point - (parent.position + child.position)) { child.fromAbsolute (point) }

        expect(child.fromAbsolute(point)) { child.toLocal(point, root) }
    }

    @Test @JsName("toLocalWorks")
    fun `to local works`() {
        val root   = gizmo()
        val parent = gizmo().apply { x += 10.0; y += 12.0 }
        val child1 = gizmo().apply { x += 10.0; y += 12.0 }
        val child2 = gizmo().apply { x += 20.0; y += 12.0 }

        root.children_   += parent
        parent.children_ += child1
        parent.children_ += child2

        expect(Origin                           ) { parent.toLocal(parent.position,    root  ) }
        expect(Point(-45.0, 0.89)               ) { root.toLocal  (Point(-45.0, 0.89), root  ) }
        expect(child2.position - child1.position) { child1.toLocal(Origin,             child2) }
    }

    @Test @JsName("childAtWorks")
    fun `child at works`() {
        val root   = gizmo()
        val child0 = gizmo().apply { x += 10.0; y += 12.0 }
        val child1 = gizmo().apply { x += 10.0; y += 12.0 }
        val child2 = gizmo().apply { x += 20.0; y += 12.0 }
        val child3 = gizmo().apply { x += 10.0; y += 23.0; width = 0.0 }

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

    @Test @JsName("zIndexWorks")
    fun `z-index works`() {
        val root   = gizmo()
        val child0 = gizmo().apply { x += 10.0; y += 12.0 }
        val child1 = gizmo().apply { x += 10.0; y += 12.0 }
        val child2 = gizmo().apply { x += 20.0; y += 12.0 }
        val child3 = gizmo().apply { x += 10.0; y += 23.0; width = 0.0 }

        root.children_ += child0
        root.children_ += child1
        root.children_ += child2
        root.children_ += child3

        expect(3) { root.zIndex_(child0) }
        expect(2) { root.zIndex_(child1) }
        expect(1) { root.zIndex_(child2) }
        expect(0) { root.zIndex_(child3) }

        root.children_.move(child0, 3)

        expect(3) { root.zIndex_(child1) }
        expect(2) { root.zIndex_(child2) }
        expect(1) { root.zIndex_(child3) }
        expect(0) { root.zIndex_(child0) }
    }

    private fun validateFocusChanged(event: FocusEvent, block: (Gizmo, PropertyObserver<Gizmo, Boolean>, FocusEvent) -> Unit) {
        val gizmo    = object: Gizmo() {}
        val observer = mockk<PropertyObserver<Gizmo, Boolean>>(relaxed = true)

        gizmo.focusChanged += observer

        // Force the Gizmo to have focus if we are testing losing it
        if (event.type == Lost) {
            gizmo.handleFocusEvent(mockk<FocusEvent>(relaxed = true).apply { every { type } returns Gained })
        }

        gizmo.handleFocusEvent(event)

        block(gizmo, observer, event)
    }

    private fun validateMouseChanged(event: MouseEvent, block: (MouseListener, MouseEvent) -> Unit) {
        val gizmo    = object: Gizmo() {}
        val listener = mockk<MouseListener>(relaxed = true)

        gizmo.mouseChanged += listener

        gizmo.handleMouseEvent_(event)

        block(listener, event)
    }

    private fun validateMouseMotionChanged(event: MouseEvent, block: (MouseMotionListener, MouseEvent) -> Unit) {
        val gizmo    = object: Gizmo() {}
        val listener = mockk<MouseMotionListener>(relaxed = true)

        gizmo.mouseMotionChanged += listener

        gizmo.handleMouseMotionEvent_(event)

        block(listener, event)
    }

    private fun validateChanged(property: KMutableProperty1<Gizmo, Boolean>, changed: KProperty1<Gizmo, PropertyObservers<Gizmo, Boolean>>) {
        val gizmo    = object: Gizmo() {}
        val old      = property.get(gizmo)
        val observer = mockk<PropertyObserver<Gizmo, Boolean>>(relaxed = true)

        changed.get(gizmo).plusAssign(observer)

        property.set(gizmo, !property.get(gizmo))

        verify(exactly = 1) { observer(gizmo, old, property.get(gizmo)) }
    }

    private fun <T> validateDefault(p: KProperty1<Gizmo, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(object: Gizmo() {}) }
    }

    private fun <T> validateSetter(p: KMutableProperty1<Gizmo, T>, value: T) {
        object: Gizmo() {}.also {
            p.set(it, value)

            expect(value, "$p set to $value") { p.get(it) }
        }
    }

    private fun gizmo(): Gizmo = object: Gizmo() {}.apply { bounds = Rectangle(size = Size(10.0, 10.0)) }
}