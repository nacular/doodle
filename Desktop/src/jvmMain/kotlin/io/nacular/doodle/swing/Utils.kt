package io.nacular.doodle.swing

import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.Cursor.Companion.Crosshair
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.system.Cursor.Companion.EResize
import io.nacular.doodle.system.Cursor.Companion.Grab
import io.nacular.doodle.system.Cursor.Companion.Move
import io.nacular.doodle.system.Cursor.Companion.NResize
import io.nacular.doodle.system.Cursor.Companion.NeResize
import io.nacular.doodle.system.Cursor.Companion.NwResize
import io.nacular.doodle.system.Cursor.Companion.SResize
import io.nacular.doodle.system.Cursor.Companion.SeResize
import io.nacular.doodle.system.Cursor.Companion.SwResize
import io.nacular.doodle.system.Cursor.Companion.Text
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.system.Cursor.Companion.Wait
import io.nacular.doodle.system.Cursor.Companion.custom
import org.jetbrains.skiko.SkiaLayer
import java.awt.Cursor
import java.awt.event.MouseEvent

/**
 * Created by Nicholas Eddy on 10/4/21.
 */
internal fun MouseEvent.location(window: SkiaLayer): Point {
    val windowScreenLocation = window.locationOnScreen
    return locationOnScreen.run { Point(x - windowScreenLocation.x, y - windowScreenLocation.y) }
}

internal fun Cursor.doodle() = when (type) {
    Cursor.TEXT_CURSOR      -> Text
    Cursor.WAIT_CURSOR      -> Wait
    Cursor.MOVE_CURSOR      -> Move
    Cursor.HAND_CURSOR      -> Grab
    Cursor.DEFAULT_CURSOR   -> Default
    Cursor.N_RESIZE_CURSOR  -> NResize
    Cursor.S_RESIZE_CURSOR  -> SResize
    Cursor.E_RESIZE_CURSOR  -> EResize
    Cursor.W_RESIZE_CURSOR  -> WResize
    Cursor.NE_RESIZE_CURSOR -> NeResize
    Cursor.NW_RESIZE_CURSOR -> NwResize
    Cursor.SE_RESIZE_CURSOR -> SeResize
    Cursor.SW_RESIZE_CURSOR -> SwResize
    Cursor.CROSSHAIR_CURSOR -> Crosshair
    else                    -> custom(name, or = Default)
}