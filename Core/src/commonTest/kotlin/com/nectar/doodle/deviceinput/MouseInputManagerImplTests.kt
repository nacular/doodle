package com.nectar.doodle.deviceinput

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Box
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
import com.nectar.doodle.system.SystemMouseEvent.Type.Click
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Drag
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import io.mockk.Ordering.ORDERED
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 2/27/18.
 */
@Suppress("FunctionName")
class MouseInputManagerImplTests {
    @Test @JsName("correctDefaultCursorOnInit")
    fun `correct default cursor on init`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService        += manager }
        verify(exactly = 1) { inputService.cursor  = Default }
    }

    @Test @JsName("displayCursorOnInit")
    fun `display cursor on init`() {
        val display      = display(Help)
        val inputService = mockk<MouseInputService>()

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService        += manager }
        verify(exactly = 1) { inputService.cursor  = Help    }
    }

    @Test @JsName("handlesDisplayCursorChanges")
    fun `handles display cursor changes`() {
        val display      = display(Help)
        val inputService = mockk<MouseInputService>()

        lateinit var cursorChanged: (Display, Cursor?, Cursor?) -> Unit

        every { display.cursorChanged += captureLambda() } answers {
            cursorChanged = lambda<(Display, Cursor?, Cursor?) -> Unit>().captured
        }

        MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        cursorChanged(display, Help, Move)

        verify(exactly = 1) { inputService.cursor = Move }
    }

    @Test @JsName("cursorOnMouseEnterExitViewDisplay")
    fun `correct cursor on mouse-enter,exit view - display`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = view(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService.cursor = Default }

        manager.changed(SystemMouseEvent(Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 1) { inputService.cursor = Move }

        manager.changed(SystemMouseEvent(Type.Move, Point(11.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 2) { inputService.cursor = Default }
    }

    @Test @JsName("correctCursorOnCoveredViewCursorChanged")
    fun `correct cursor on covered view cursor changed`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = view(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

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

    @Test @JsName("displayCursorOverridesCoveredView")
    fun `display cursor overrides covered view`() {
        val display      = display(Progress)
        val inputService = mockk<MouseInputService>()
        val child        = view(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

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

    @Test @JsName("mouseDownNoHit")
    fun `mouse down, no hit`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.mouseChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Down, Point(-10.0, -10.0), setOf(Button1), 2, emptySet()))

        verify(atLeast = 2) { inputService.toolTipText = "" }

        verify(exactly = 0) { child.handleMouseEvent_(any()) }
    }

    @Test @JsName("mouseDownDisabled")
    fun `mouse down, disabled`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.mouseChanged += mockk()

        every { display.child(any()) } returns child
        every { child.enabled        } returns false

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 0) { child.handleMouseEvent_(any()) }
    }

    @Test @JsName("mouseDownDisabledGoesToParent")
    fun `mouse down, disabled goes to parent`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val parent       = spyk(Box())
        val child        = spyk(view())

        every { display.child(any()) } returns parent
        every { parent.child (any()) } returns child
        every { child.parent         } returns parent
        every { child.enabled        } returns false

        parent.children  += child
        display.children += parent

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 0) { child.handleMouseEvent_(any()) }

        verify(ORDERED) {
            parent.handleMouseEvent_(MouseEvent(parent, parent, Enter, Point(10.0, 10.0), Button1, 1, emptySet()))
            parent.handleMouseEvent_(MouseEvent(parent, parent, Down,  Point(10.0, 10.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("mouseDownInformsHandler")
    fun `mouse down, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, child, Enter, Point(1.0, 1.0), Button1, 2, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Down,  Point(1.0, 1.0), Button1, 2, emptySet()))
        }
    }

    @Test @JsName("mouseDownInformsParentHandler")
    fun `mouse down, informs parent handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val parent       = spyk(view())
        val child        = spyk(view())

        parent.position = Point(9.0, 9.0)

        parent.children_ += child

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns parent
        every { child.parent                     } returns parent

        display.children += parent

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            parent.handleMouseEvent_(MouseEvent(parent, child, Enter, Point(1.0, 1.0), Button1, 2, emptySet()))
            parent.handleMouseEvent_(MouseEvent(parent, child, Down,  Point(1.0, 1.0), Button1, 2, emptySet()))
        }
    }

    @Test @JsName("mouseDragInformsHandler")
    fun `mouse drag, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position   = Point(9.0, 9.0)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down,      Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Move, Point(20.0, 20.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_      (MouseEvent(child, child, Down, Point( 1.0,  1.0), Button1, 1, emptySet()))
            child.handleMouseMotionEvent_(MouseEvent(child, child, Drag, Point(11.0, 11.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("mouseMoveInformsHandler")
    fun `mouse move, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position            = Point(9.0, 9.0)
        child.mouseChanged       += mockk()
        child.mouseMotionChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child
        every { display.child(Point(20.0, 20.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Type.Move, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Type.Move, Point(20.0, 20.0), setOf(Button1), 1, emptySet()))

        verify(ORDERED) {
            child.handleMouseEvent_      (MouseEvent(child, child, Enter,     Point( 1.0,  1.0), Button1, 1, emptySet()))
            child.handleMouseMotionEvent_(MouseEvent(child, child, Type.Move, Point(11.0, 11.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("singleClickInformsHandler")
    fun `single-click, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position      = Point(9.0, 9.0)
        child.mouseChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Up,   Point(10.0, 10.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, child, Down,  Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Click, Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("downUpOutsideInformsHandler")
    fun `down, up outside, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position      = Point(9.0, 9.0)
        child.mouseChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Up,   Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, child, Down, Point(  1.0,   1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Up,   Point(-19.0, -19.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Exit, Point(-19.0, -19.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("downOutsideUpInsideInformsHandler")
    fun `down outside, up inside, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))
        manager.changed(SystemMouseEvent(Up,   Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, child, Enter, Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("upInsideInformsHandler")
    fun `up inside, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Up, Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, child, Enter, Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("doubleClickInformsHandler")
    fun `double-click, informs handler`() {
        val display      = display()
        val inputService = mockk<MouseInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.mouseChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = MouseInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager.changed(SystemMouseEvent(Up, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handleMouseEvent_(MouseEvent(child, child, Up,    Point(1.0, 1.0), Button1, 2, emptySet()))
            child.handleMouseEvent_(MouseEvent(child, child, Click, Point(1.0, 1.0), Button1, 2, emptySet()))
        }
    }

    private fun display(cursor: Cursor? = null) = mockk<Display>().apply {
        every { this@apply.cursor } returns cursor
    }

    private fun view(cursor: Cursor? = null, bounds: Rectangle = Rectangle(size = Size(100.0, 100.0))): View {
        return object: View() {}.apply {
            this.bounds = bounds
            this.cursor = cursor
        }
    }
}