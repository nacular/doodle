package io.nacular.doodle.deviceinput

import JsName
import io.mockk.Ordering.ORDERED
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.event.Interaction
import io.nacular.doodle.event.Pointer
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Crosshair
import io.nacular.doodle.system.Cursor.Companion.Help
import io.nacular.doodle.system.Cursor.Companion.Move
import io.nacular.doodle.system.Cursor.Companion.Progress
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.SystemInputEvent.Modifier
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button
import io.nacular.doodle.system.SystemPointerEvent.Button.Button1
import io.nacular.doodle.system.SystemPointerEvent.Type
import io.nacular.doodle.system.SystemPointerEvent.Type.Click
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Enter
import io.nacular.doodle.system.SystemPointerEvent.Type.Exit
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.utils.PropertyObserver
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 2/27/18.
 */
class PointerInputManagerImplTests {
    @Test @JsName("correctDefaultCursorOnInit")
    fun `correct default cursor on init`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService        += manager }
        verify(exactly = 1) { inputService.cursor  = null    }
    }

    @Test @JsName("displayCursorOnInit")
    fun `display cursor on init`() {
        val display      = display(Help)
        val inputService = mockk<PointerInputService>()

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService        += manager }
        verify(exactly = 1) { inputService.cursor  = Help    }
    }

    @Test @JsName("handlesDisplayCursorChanges")
    fun `handles display cursor changes`() {
        val display      = display(Help)
        val inputService = mockk<PointerInputService>()

        lateinit var cursorChanged: (Display, Cursor?, Cursor?) -> Unit

        every { display.cursorChanged += captureLambda() } answers {
            cursorChanged = lambda<(Display, Cursor?, Cursor?) -> Unit>().captured
        }

        PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        cursorChanged(display, Help, Move)

        verify(exactly = 1) { inputService.cursor = Move }
    }

    @Test @JsName("cursorOnPointerEnterExitViewDisplay")
    fun `correct cursor on pointer-enter,exit view - display`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = view(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService.cursor = null }

        manager(SystemPointerEvent(0, Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 1) { inputService.cursor = Move }

        manager(SystemPointerEvent(0, Type.Move, Point(11.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 2) { inputService.cursor = null }
    }

    @Test @JsName("correctCursorOnCoveredViewCursorChanged")
    fun `correct cursor on covered view cursor changed`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = view(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService.cursor = null }

        manager(SystemPointerEvent(0, Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 1) { inputService.cursor = Move }

        child.cursor = Crosshair

        verify(exactly = 1) { inputService.cursor = Crosshair }

        child.cursor = null

        verify(exactly = 2) { inputService.cursor = null }

        child.cursor = Crosshair

        verify(exactly = 2) { inputService.cursor = Crosshair }
    }

    @Test @JsName("displayCursorOverridesCoveredView")
    fun `display cursor overrides covered view`() {
        val display      = display(Progress)
        val inputService = mockk<PointerInputService>()
        val child        = view(Move)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        verify(exactly = 1) { inputService.cursor = Progress }

        manager(SystemPointerEvent(0, Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(exactly = 2) { inputService.cursor = Progress }

        child.cursor = Crosshair

        verify(exactly = 3) { inputService.cursor = Progress }

        child.cursor = null

        verify(exactly = 4) { inputService.cursor = Progress }

        child.cursor = Crosshair

        verify(exactly = 5) { inputService.cursor = Progress }
    }

    @Test @JsName("pointerDownNoHit")
    fun `pointer down, no hit`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.pointerChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))
        manager(SystemPointerEvent(0, Down, Point(-10.0, -10.0), setOf(Button1), 2, emptySet()))

        verify(atLeast = 2) { inputService.toolTipText = "" }

        verify(exactly = 0) { child.handlePointerEvent_(any()) }
    }

    @Test @JsName("noEventPointerDownDisabled")
    fun `no event pointer down, disabled`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.pointerChanged += mockk()

        every { display.child(any()) } returns child
        every { child.enabled        } returns false

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 0) {
            child.filterPointerEvent_(any())
            child.handlePointerEvent_(any())
        }
    }

    @Test @JsName("pointerDownDisabledGoesToParent")
    fun `pointer down, disabled goes to parent`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val grandParent  = spyk(Container())
        val parent       = spyk(Container())
        val child        = spyk(view())

        every { display.child     (any()) } returns grandParent
        every { grandParent.child (any()) } returns parent
        every { parent.child      (any()) } returns child
        every { parent.parent             } returns grandParent
        every { child.parent              } returns parent
        every { parent.enabled            } returns false
        every { child.enabled             } returns false

        parent.children      += child
        grandParent.children += parent
        display.children     += grandParent

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))

        verify(exactly = 0) {
            child.filterPointerEvent_ (any())
            child.handlePointerEvent_ (any())
        }
        verify(exactly = 0) {
            parent.filterPointerEvent_(any())
            parent.handlePointerEvent_(any())
        }

        verify(ORDERED) {
            grandParent.filterPointerEvent_(pointerEvent(grandParent, grandParent, id = 0, Enter, Point(10.0, 10.0), Button1, 1, emptySet()))
            grandParent.handlePointerEvent_(pointerEvent(grandParent, grandParent, id = 0, Enter, Point(10.0, 10.0), Button1, 1, emptySet()))

            grandParent.filterPointerEvent_(pointerEvent(grandParent, grandParent, id = 0, Down,  Point(10.0, 10.0), Button1, 1, emptySet()))
            grandParent.handlePointerEvent_(pointerEvent(grandParent, grandParent, id = 0, Down,  Point(10.0, 10.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("pointerDownInformsHandler")
    fun `pointer down, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.pointerChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Enter, Point(1.0, 1.0), Button1, 2, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Down,  Point(1.0, 1.0), Button1, 2, emptySet()))
        }
    }

    @Test @JsName("pointerDownInformsParentHandler")
    fun `pointer down, informs parent handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val parent       = spyk(view())
        val child        = spyk(view())

        parent.position = Point(9.0, 9.0)

        parent.children_ += child

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns parent
        every { child.parent                     } returns parent

        display.children += parent

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            parent.filterPointerEvent_(pointerEvent(parent, child, id = 0, Enter, Point(1.0, 1.0), Button1, 2, emptySet()))
            parent.handlePointerEvent_(pointerEvent(parent, child, id = 0, Enter, Point(1.0, 1.0), Button1, 2, emptySet()))

            parent.filterPointerEvent_(pointerEvent(parent, child, id = 0, Down,  Point(1.0, 1.0), Button1, 2, emptySet()))
            parent.handlePointerEvent_(pointerEvent(parent, child, id = 0, Down,  Point(1.0, 1.0), Button1, 2, emptySet()))
        }
    }

    @Test @JsName("pointerDragInformsHandler")
    fun `pointer drag, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position   = Point(9.0, 9.0)

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down,      Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager(SystemPointerEvent(0, Type.Move, Point(20.0, 20.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handlePointerEvent_      (pointerEvent(child, child, id = 0, Down, Point( 1.0,  1.0), Button1, 1, emptySet()))
            child.handlePointerMotionEvent_(pointerEvent(child, child, id = 0, Drag, Point(11.0, 11.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("pointerMoveInformsHandler")
    fun `pointer move, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position            = Point(9.0, 9.0)
        child.pointerChanged       += mockk()
        child.pointerMotionChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child
        every { display.child(Point(20.0, 20.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Type.Move, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager(SystemPointerEvent(0, Type.Move, Point(20.0, 20.0), setOf(Button1), 1, emptySet()))

        verify(ORDERED) {
            child.handlePointerEvent_      (pointerEvent(child, child, id = 0, Enter,     Point( 1.0,  1.0), Button1, 1, emptySet()))
            child.handlePointerMotionEvent_(pointerEvent(child, child, id = 0, Type.Move, Point(11.0, 11.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("singleClickInformsHandler")
    fun `single-click, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position      = Point(9.0, 9.0)
        child.pointerChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point(10.0, 10.0), setOf(Button1), 1, emptySet()))
        manager(SystemPointerEvent(0, Up,   Point(10.0, 10.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Down,  Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Click, Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("downUpOutsideInformsHandler")
    fun `down, up outside, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position      = Point(9.0, 9.0)
        child.pointerChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))
        manager(SystemPointerEvent(0, Up,   Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Down, Point(  1.0,   1.0), Button1, 1, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Up,   Point(-19.0, -19.0), Button1, 1, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Exit, Point(-19.0, -19.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("downOutsideUpInsideInformsHandler")
    fun `down outside, up inside, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.pointerChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Down, Point(-10.0, -10.0), setOf(Button1), 1, emptySet()))
        manager(SystemPointerEvent(0, Up,   Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))

        verify(atLeast = 1) { inputService.toolTipText = "" }

        verify(ORDERED) {
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Enter, Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("upInsideInformsHandler")
    fun `up inside, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.pointerChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Up, Point( 10.0,  10.0), setOf(Button1), 1, emptySet()))

        verify(ORDERED) {
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Enter, Point(1.0, 1.0), Button1, 1, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Up,    Point(1.0, 1.0), Button1, 1, emptySet()))
        }
    }

    @Test @JsName("doubleClickInformsHandler")
    fun `double-click, informs handler`() {
        val display      = display()
        val inputService = mockk<PointerInputService>()
        val child        = spyk(view())

        child.position = Point(9.0, 9.0)
        child.pointerChanged += mockk()

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Up, Point(10.0, 10.0), setOf(Button1), 2, emptySet()))

        verify(ORDERED) {
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Up,    Point(1.0, 1.0), Button1, 2, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Click, Point(1.0, 1.0), Button1, 2, emptySet()))
        }
    }

    @Test @JsName("pointersCleanedUpWhenViewDisabled")
    fun `cleans up pointers when view disabled`() {
        val enabledChanged = slot<PropertyObserver<View, Boolean>>()
        val display        = display()
        val inputService   = mockk<PointerInputService>()
        val child          = focusableView().apply {
            every { this@apply.enabledChanged += capture(enabledChanged) } just Runs
        }

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Enter, Point(10.0, 10.0), emptySet(), 0, emptySet()))
        manager(SystemPointerEvent(0, Down,  Point(10.0, 10.0), emptySet(), 0, emptySet()))

        enabledChanged.captured(child, true, false)

        manager(SystemPointerEvent(0, Type.Move, Point( 9.0,  9.0), emptySet(), 0, emptySet()))

        enabledChanged.captured(child, false, true)

        manager(SystemPointerEvent(1, Down, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify(ORDERED) {
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Enter, Point(1.0, 1.0), emptySet(), 0, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 0, Down,  Point(1.0, 1.0), emptySet(), 0, emptySet()))
            child.handlePointerEvent_(pointerEvent(child, child, id = 1, Down,  Point(1.0, 1.0), emptySet(), 0, emptySet()))
        }
    }

    @Test
    fun `cleans up listeners on exit`() {
        val cursorChanged    = slot<PropertyObserver<View, Cursor?>>()
        val enabledChanged   = slot<PropertyObserver<View, Boolean>>()
        val boundsChanged    = slot<PropertyObserver<View, Rectangle>>()
        val transformChanged = slot<PropertyObserver<View, AffineTransform>>()
        val display          = display()
        val inputService     = mockk<PointerInputService>()
        val child            = focusableView().apply {
            every { this@apply.cursorChanged    += capture(cursorChanged   ) } just Runs
            every { this@apply.enabledChanged   += capture(enabledChanged  ) } just Runs
            every { this@apply.boundsChanged    += capture(boundsChanged   ) } just Runs
            every { this@apply.transformChanged += capture(transformChanged) } just Runs
        }

        every { display.child(any()            ) } returns null
        every { display.child(Point(10.0, 10.0)) } returns child

        display.children += child

        val manager = PointerInputManagerImpl(display, inputService, ViewFinderImpl(display))

        manager(SystemPointerEvent(0, Type.Move, Point(10.0, 10.0), emptySet(), 0, emptySet()))

        verify {
            child.cursorChanged    += any()
            child.enabledChanged   += any()
            child.boundsChanged    += any()
            child.transformChanged += any()
        }

        manager(SystemPointerEvent(0, Type.Move, Point(20.0, 20.0), emptySet(), 0, emptySet()))

        verify {
            child.cursorChanged    -= cursorChanged.captured
            child.enabledChanged   -= enabledChanged.captured
            child.boundsChanged    -= boundsChanged.captured
            child.transformChanged -= transformChanged.captured
        }
    }

    private fun focusableView() = mockk<View>().apply {
        every { parent                } returns null
        every { enabled               } returns true
        every { visible               } returns true
        every { focusable             } returns true
        every { focusCycleRoot_       } returns null
        every { shouldYieldFocus()    } returns true
        every { focusTraversalPolicy_ } returns null
    }

    private fun display(cursor: Cursor? = null) = mockk<Display>().apply {
        every { this@apply.cursor } returns cursor
    }

    private fun view(cursor: Cursor? = null, bounds: Rectangle = Rectangle(size = Size(100.0, 100.0))): View = view {
        this.bounds = bounds
        this.cursor = cursor
    }

    private fun pointerEvent(source: View, target: View, id: Int, type: Type, location: Point, button: Button, clickCount: Int, modifiers: Set<Modifier>) = pointerEvent(
        source, target, id, type, location, setOf(button), clickCount, modifiers
    )

    private fun pointerEvent(source: View, target: View, id: Int, type: Type, location: Point, buttons: Set<Button>, clickCount: Int, modifiers: Set<Modifier>): PointerEvent {
        val interactions = setOf(Interaction(Pointer(id), target, type, location, location))

        return pointerEvent(source, target, interactions, interactions.toSet(), { interactions }, buttons, clickCount, modifiers)
    }

    private fun pointerEvent(
            source: View,
            target: View,
            targetInteractions: Set<Interaction>,
            changedInteractions: Set<Interaction>,
            allInteractions: () -> Set<Interaction>,
            buttons: Set<Button>,
            clickCount: Int,
            modifiers: Set<Modifier>
    ): PointerEvent {
        return PointerEvent(
                source,
                target,
                buttons,
                clickCount,
                targetInteractions,
                changedInteractions,
                allInteractions,
                modifiers
        )
    }
}