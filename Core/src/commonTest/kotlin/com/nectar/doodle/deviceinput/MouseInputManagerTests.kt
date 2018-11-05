package com.nectar.doodle.deviceinput

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.Cursor.Companion.Crosshair
import com.nectar.doodle.system.Cursor.Companion.Default
import com.nectar.doodle.system.Cursor.Companion.Help
import com.nectar.doodle.system.Cursor.Companion.Move
import com.nectar.doodle.system.Cursor.Companion.Progress
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.system.SystemMouseEvent.Type
import io.mockk.Ordering.ORDERED
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 2/27/18.
 */
class MouseInputManagerTests {
    @Test @JsName("correctDefaultCursorOnInit")
    fun `correct default cursor on init`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)

        val manager = MouseInputManager(display, inputService)

        verify(exactly = 1) { inputService        += manager }
        verify(exactly = 1) { inputService.cursor  = Default }
    }

    @Test @JsName("displayCursorOnInit")
    fun `display cursor on init`() {
        val display      = display(Help)
        val inputService = mockk<MouseInputService>(relaxed = true)

        val manager = MouseInputManager(display, inputService)

        verify(exactly = 1) { inputService        += manager     }
        verify(exactly = 1) { inputService.cursor  = Help }
    }

    @Test @JsName("handlesDisplayCursorChanges")
    fun `handles display cursor changes`() {
        val display      = display(Help)
        val inputService = mockk<MouseInputService>(relaxed = true)

        lateinit var cursorChanged: (Display, Cursor?, Cursor?) -> Unit

        every { display.cursorChanged += captureLambda() } answers {
            cursorChanged = lambda<(Display, Cursor?, Cursor?) -> Unit>().captured
        }

        MouseInputManager(display, inputService)

        cursorChanged(display, Help, Move)

        verify(exactly = 1) { inputService.cursor = Move }
    }

    @Test @JsName("cursorOnMouseEnterExitGizmoDisplay")
    fun `correct cursor on mouse-enter,exit gizmo - display`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = gizmo(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        verify(exactly = 1) { inputService.cursor = Default }

        manager.changed(SystemMouseEvent(Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 1) { inputService.cursor = Move }

        manager.changed(SystemMouseEvent(Type.Move, Point(11.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 2) { inputService.cursor = Default }
    }

    @Test @JsName("correctCursorOnCoveredGizmoCursorChanged")
    fun `correct cursor on covered gizmo cursor changed`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = gizmo(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        verify(exactly = 1) { inputService.cursor = Default }

        manager.changed(SystemMouseEvent(Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 1) { inputService.cursor = Move }

        child.cursor = Crosshair

        verify(exactly = 1) { inputService.cursor = Crosshair }

        child.cursor = null

        verify(exactly = 2) { inputService.cursor = Default }

        child.cursor = Crosshair

        verify(exactly = 2) { inputService.cursor = Crosshair }
    }

    @Test @JsName("displayCursorOverridesCoveredGizmo")
    fun `display cursor overrides covered gizmo`() {
        val display      = display(Progress)
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = gizmo(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        verify(exactly = 1) { inputService.cursor = Progress }

        manager.changed(SystemMouseEvent(Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 2) { inputService.cursor = Progress }

        child.cursor = Crosshair

        verify(exactly = 3) { inputService.cursor = Progress }

        child.cursor = null

        verify(exactly = 4) { inputService.cursor = Progress }

        child.cursor = Crosshair

        verify(exactly = 5) { inputService.cursor = Progress }
    }

    @Test @JsName("mouseDownNoHandler")
    fun `mouse down, no handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Down, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(exactly = 2) { inputService.toolTipText = "" }

        verify(exactly = 0) { child.handleMouseEvent_(any()) }
    }

    @Test @JsName("mouseDownNoHit")
    fun `mouse down, no hit`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Down, Point(-10.0, -10.0), setOf(Button1), 2, emptySet()))

        verify(exactly = 2) { inputService.toolTipText = "" }

        verify(exactly = 0) { child.handleMouseEvent_(any()) }
    }

    @Test @JsName("mouseDownInformsHandler")
    fun `mouse down, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(exactly = 1) { inputService.toolTipText = "" }

        child.handleMouseEvent_(MouseEvent(child, Type.Down,  Point(1.0, 1.0), Button1, 2, emptySet()))
    }

    @Test @JsName("mouseDownInformsParentHandler")
    fun `mouse down, informs parent handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val parent       = spyk(gizmo())
        val child        = spyk(gizmo())

        parent.position = Point(9.0, 9.0)

        parent.children_    += child
        parent.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns parent

        display.children += parent

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(exactly = 1) { inputService.toolTipText = "" }

        parent.handleMouseEvent_(MouseEvent(child, Type.Down,  Point(1.0, 1.0), Button1, 2, emptySet()))
    }

    @Test @JsName("mouseDragInformsHandler")
    fun `mouse drag, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position            = Point(9.0, 9.0)
        child.mouseChanged       += mockk(relaxed = true)
        child.mouseMotionChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Move, Point(20.0, 20.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_      (MouseEvent(child, Type.Down, Point( 1.0,  1.0), Button1, 1, emptySet()))
            child.handleMouseMotionEvent_(MouseEvent(child, Type.Drag, Point(11.0, 11.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("mouseMoveInformsHandler")
    fun `mouse move, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position            = Point(9.0, 9.0)
        child.mouseMotionChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(20.0, 20.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Move, Point(20.0, 20.0), setOf(Button1), 1, emptySet()))

        verify(ORDERED) {
            child.handleMouseMotionEvent_(MouseEvent(child, Type.Move, Point(11.0, 11.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("singleClickInformsHandler")
    fun `single-click, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Up,   Point(10.0, 10.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, Type.Down,  Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, Type.Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, Type.Click, Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("downUpOutsideInformsHandler")
    fun `down, up outside, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Up,   Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, Type.Down, Point(  1.0,   1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, Type.Up,   Point(-19.0, -19.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, Type.Exit, Point(-19.0, -19.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("downOutsideUpInsideInformsHandler")
    fun `down outside, up inside, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Up,   Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, Type.Enter, Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, Type.Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("upInsideInformsHandler")
    fun `up inside, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Up, Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, Type.Enter, Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, Type.Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("doubleClickInformsHandler")
    fun `double-click, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>(relaxed = true)
        val child        = spyk(gizmo())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk(relaxed = true)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManager(display, inputService)

        manager.changed(SystemMouseEvent(Type.Up, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(exactly = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, Type.Up,    Point(1.0, 1.0), Button1, 2, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, Type.Click, Point(1.0, 1.0), Button1, 2, emptySet()))
        }
    }

    private fun display(cursor: Cursor? = null) = mockk<Display>(relaxed = true).apply {
        every { this@apply.cursor } returns cursor
    }

    private fun gizmo(cursor: Cursor? = null, bounds: Rectangle = Rectangle(size = Size(100.0, 100.0))): View {
        return object: View() {}.apply {
            this.bounds = bounds
            this.cursor = cursor
        }
    }
}