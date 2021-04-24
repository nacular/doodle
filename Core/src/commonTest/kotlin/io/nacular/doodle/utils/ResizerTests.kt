package io.nacular.doodle.utils

import io.nacular.doodle.core.View
import io.nacular.doodle.core.view
import io.nacular.doodle.event.Interaction
import io.nacular.doodle.event.Pointer
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button.Button1
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/19/21.
 */
class ResizerTests {
    @Test @JsName("moveWorks") fun `move works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  6)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 10)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(10, 10)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor }
        expect(Rectangle(14, 14, 100, 100)) { view.bounds }
    }

    @Test @JsName("northResizeWorks") fun `n-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  0)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 10)), secondInteraction)))

        expect(Cursor.NResize) { view.cursor }
        expect(Rectangle(10, 20, 100, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, -20)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(10,   0)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 0, 100, 110)) { view.bounds }
    }

    @Test @JsName("northEastResizeWorks") fun `ne-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(100,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  0)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(110, 10)), secondInteraction)))

        expect(Cursor.NeResize) { view.cursor }
        expect(Rectangle(10, 20, 110, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point( 90, -20)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point( 90,   0)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 0, 90, 110)) { view.bounds }
    }

    @Test @JsName("eastResizeWorks") fun `e-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(100,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  6)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(110, 90)), secondInteraction)))

        expect(Cursor.EResize) { view.cursor }
        expect(Rectangle(10, 10, 110, 100)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point( 90,  6)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point( 90,  6)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 10, 90, 100)) { view.bounds }
    }

    @Test @JsName("southEastResizeWorks") fun `se-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(100, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100, 100)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(110, 110)), secondInteraction)))

        expect(Cursor.SeResize) { view.cursor }
        expect(Rectangle(10, 10, 110, 110)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point( 90,  90)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point( 90,  90)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 10, 90, 90)) { view.bounds }
    }

    @Test @JsName("southResizeWorks") fun `s-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 6, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6, 100)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10,  90)), secondInteraction)))

        expect(Cursor.SResize) { view.cursor }
        expect(Rectangle(10, 10, 100, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 110)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(10, 110)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 10, 100, 110)) { view.bounds }
    }

    @Test @JsName("southWestResizeWorks") fun `sw-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 0, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0, 100)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 110)), secondInteraction)))

        expect(Cursor.SwResize) { view.cursor }
        expect(Rectangle(20, 10, 90, 110)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(-20, 90)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(-20, 90)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(0, 10, 110, 90)) { view.bounds }
    }

    @Test @JsName("westResizeWorks") fun `w-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(0,   6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(0,   6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(0,   6)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 90)), secondInteraction)))

        expect(Cursor.WResize) { view.cursor }
        expect(Rectangle(20, 10, 90, 100)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(-20,  6)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(-20,  6)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(0, 10, 110, 100)) { view.bounds }
    }

    @Test @JsName("northWestResizeWorks") fun `nw-resize works`() {
        val view              = view { bounds = Rectangle(10, 10, 100, 100) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 0,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0,  0)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 10)), secondInteraction)))

        expect(Cursor.NwResize) { view.cursor }
        expect(Rectangle(20, 20, 90, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(-20, -20)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(-20, -20)), secondInteraction)))

        expect(Cursor.Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(0, 0, 110, 110)) { view.bounds }
    }

    private fun event(
        source             : View,
        changedInteractions: Set<Interaction>,
        targetInteractions : Set<Interaction> = changedInteractions) = PointerEvent(
            source,
            source,
            setOf(Button1),
            1,
            targetInteractions,
            changedInteractions,
            { targetInteractions },
            emptySet()
    )

    private fun interaction(
            pointer         : Pointer,
            target          : View,
            state           : SystemPointerEvent.Type,
            location        : Point,
            absoluteLocation: Point = location) = Interaction(pointer, target, state, location, absoluteLocation)
}