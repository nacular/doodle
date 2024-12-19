package io.nacular.doodle.utils

import io.nacular.doodle.core.View
import io.nacular.doodle.core.forceBounds
import io.nacular.doodle.core.view
import io.nacular.doodle.event.Interaction
import io.nacular.doodle.event.Pointer
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.EResize
import io.nacular.doodle.system.Cursor.Companion.Grabbing
import io.nacular.doodle.system.Cursor.Companion.NResize
import io.nacular.doodle.system.Cursor.Companion.NeResize
import io.nacular.doodle.system.Cursor.Companion.NwResize
import io.nacular.doodle.system.Cursor.Companion.SResize
import io.nacular.doodle.system.Cursor.Companion.SeResize
import io.nacular.doodle.system.Cursor.Companion.SwResize
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button.Button1
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Move
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 4/19/21.
 */
class ResizerTests {
    @Test fun move() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point( 6,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 6,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 6,  6)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10, 10)))))
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point(10, 10)))))

        view.syncBounds()

        expect(Cursor.Grab) { view.cursor }
        expect(Rectangle(14, 14, 100, 100)) { view.bounds }
    }

    @Test fun `move multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  6)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 10)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(10, 10)), secondInteraction)))

        view.syncBounds()

        expect(Grabbing) { view.cursor }
        expect(Rectangle(14, 14, 100, 100)) { view.bounds }
    }

    @Test fun `n-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point( 6,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 6,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 6,  0)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10, 10)))))

        view.syncBounds()

        expect(NResize) { view.cursor }
        expect(Rectangle(10, 20, 100, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10, -20)))))
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point(10,   0)))))

        view.syncBounds()

        expect(NResize) { view.cursor }
        expect(Rectangle(10, 0, 100, 110)) { view.bounds }
    }

    @Test fun `n-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6,  0)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 10)), secondInteraction)))

        view.syncBounds()

        expect(NResize) { view.cursor }
        expect(Rectangle(10, 20, 100, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, -20)), secondInteraction)))
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(10,   0)), secondInteraction)))

        view.syncBounds()

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 0, 100, 110)) { view.bounds }
    }

    @Test fun `ne-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point(100,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(100,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(100,  0)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(110, 10)))))

        view.syncBounds()

        expect(NeResize) { view.cursor }
        expect(Rectangle(10, 20, 110, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point( 90, -20))))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point( 90,   0)))))

        expect(NeResize) { view.cursor }
        expect(Rectangle(10, 0, 90, 110)) { view.bounds }
    }

    @Test fun `ne-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(100,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  0)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(110, 10)), secondInteraction))); view.syncBounds()

        expect(NeResize) { view.cursor }
        expect(Rectangle(10, 20, 110, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point( 90, -20)), secondInteraction))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point( 90,   0)), secondInteraction)))

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 0, 90, 110)) { view.bounds }
    }

    @Test fun `e-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point(100,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(100,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(100,  6)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(110, 90))))); view.syncBounds()

        expect(EResize) { view.cursor }
        expect(Rectangle(10, 10, 110, 100)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point( 90,  6))))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point( 90,  6)))))

        expect(EResize) { view.cursor }
        expect(Rectangle(10, 10, 90, 100)) { view.bounds }
    }

    @Test fun `e-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(100,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100,  6)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(110, 90)), secondInteraction))); view.syncBounds()

        expect(EResize) { view.cursor }
        expect(Rectangle(10, 10, 110, 100)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point( 90,  6)), secondInteraction))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point( 90,  6)), secondInteraction)))

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 10, 90, 100)) { view.bounds }
    }

    @Test fun `se-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point(100, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(100, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(100, 100)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(110, 110))))); view.syncBounds()

        expect(SeResize) { view.cursor }
        expect(Rectangle(10, 10, 110, 110)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point( 90,  90))))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point( 90,  90)))))

        expect(SeResize) { view.cursor }
        expect(Rectangle(10, 10, 90, 90)) { view.bounds }
    }

    @Test fun `se-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(100, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(100, 100)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(110, 110)), secondInteraction))); view.syncBounds()

        expect(SeResize) { view.cursor }
        expect(Rectangle(10, 10, 110, 110)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point( 90,  90)), secondInteraction))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point( 90,  90)), secondInteraction)))

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 10, 90, 90)) { view.bounds }
    }

    @Test fun `s-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point( 6, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 6, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 6, 100)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10,  90))))); view.syncBounds()

        expect(SResize) { view.cursor }
        expect(Rectangle(10, 10, 100, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10, 110))))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point(10, 110)))))

        expect(SResize) { view.cursor }
        expect(Rectangle(10, 10, 100, 110)) { view.bounds }
    }

    @Test fun `s-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 6, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 6, 100)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10,  90)), secondInteraction))); view.syncBounds()

        expect(SResize) { view.cursor }
        expect(Rectangle(10, 10, 100, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 110)), secondInteraction))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(10, 110)), secondInteraction)))

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(10, 10, 100, 110)) { view.bounds }
    }

    @Test fun `sw-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point( 0, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 0, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 0, 100)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10, 110))))); view.syncBounds()

        expect(SwResize) { view.cursor }
        expect(Rectangle(20, 10, 90, 110)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(-20, 90))))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point(-20, 90)))))

        expect(SwResize) { view.cursor }
        expect(Rectangle(0, 10, 110, 90)) { view.bounds }
    }

    @Test fun `sw-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 0, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0, 100)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0, 100)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 110)), secondInteraction))); view.syncBounds()

        expect(SwResize) { view.cursor }
        expect(Rectangle(20, 10, 90, 110)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(-20, 90)), secondInteraction))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(-20, 90)), secondInteraction)))

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(0, 10, 110, 90)) { view.bounds }
    }

    @Test fun `w-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point(0,   6)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(0,   6)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point(0,   6)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10, 90))))); view.syncBounds()

        expect(WResize) { view.cursor }
        expect(Rectangle(20, 10, 90, 100)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(-20,  6))))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point(-20,  6)))))

        expect(WResize) { view.cursor }
        expect(Rectangle(0, 10, 110, 100)) { view.bounds }
    }

    @Test fun `w-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point(0,   6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(0,   6)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point(0,   6)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 90)), secondInteraction))); view.syncBounds()

        expect(WResize) { view.cursor }
        expect(Rectangle(20, 10, 90, 100)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(-20,  6)), secondInteraction))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(-20,  6)), secondInteraction)))

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
        expect(Rectangle(0, 10, 110, 100)) { view.bounds }
    }

    @Test fun `nw-resize`() {
        val view    = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer = Resizer(view)
        val pointer = Pointer(0)

        resizer.entered (event(view, setOf(interaction(pointer, view, Down, Point( 0,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 0,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer, view, Down, Point( 0,  0)))))
        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(10, 10))))); view.syncBounds()

        expect(NwResize) { view.cursor }
        expect(Rectangle(20, 20, 90, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer, view, Move, Point(-20, -20))))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer, view, Up,   Point(-20, -20)))))

        expect(NwResize) { view.cursor }
        expect(Rectangle(0, 0, 110, 110)) { view.bounds }
    }

    @Test fun `nw-resize multi-touch`() {
        val view              = view { forceBounds(Rectangle(10, 10, 100, 100)) }
        val resizer           = Resizer(view)
        val pointer1          = Pointer(0)
        val pointer2          = Pointer(1)
        val secondInteraction = interaction(pointer2, view, Down, Point(25, 25))

        resizer.entered (event(view, setOf(interaction(pointer1, view, Down, Point( 0,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0,  0)))))
        resizer.pressed (event(view, setOf(interaction(pointer1, view, Down, Point( 0,  0)), secondInteraction)))
        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(10, 10)), secondInteraction))); view.syncBounds()

        expect(NwResize) { view.cursor }
        expect(Rectangle(20, 20, 90, 90)) { view.bounds }

        resizer.dragged (event(view, setOf(interaction(pointer1, view, Move, Point(-20, -20)), secondInteraction))); view.syncBounds()
        resizer.released(event(view, setOf(interaction(pointer1, view, Up,   Point(-20, -20)), secondInteraction)))

        expect(Grabbing) { view.cursor } //  since second interaction is now ready for move
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