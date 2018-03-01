package com.nectar.doodle.deviceinput

import com.nectar.doodle.JsName
import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.Cursor.Companion.Crosshair
import com.nectar.doodle.system.Cursor.Companion.Default
import com.nectar.doodle.system.Cursor.Companion.Help
import com.nectar.doodle.system.Cursor.Companion.Move
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Type
import io.mockk.every
import io.mockk.mockk
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
    }

    private fun display(cursor: Cursor? = null) = mockk<Display>(relaxed = true).apply {
        every { this@apply.cursor } returns cursor
    }

    private fun gizmo(cursor: Cursor? = null, bounds: Rectangle = Rectangle(size = Size(100.0, 100.0))): Gizmo {
        return object: Gizmo() {}.apply {
            this.bounds = bounds
            this.cursor = cursor
        }
    }
}