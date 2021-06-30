package io.nacular.doodle.theme.native

import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.system.SystemInputEvent.Modifier.Alt
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Type
import kotlinx.datetime.Clock
import java.awt.Component
import java.awt.event.InputEvent.ALT_DOWN_MASK
import java.awt.event.InputEvent.BUTTON1_DOWN_MASK
import java.awt.event.InputEvent.BUTTON2_DOWN_MASK
import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.InputEvent.META_DOWN_MASK
import java.awt.event.InputEvent.SHIFT_DOWN_MASK
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import java.awt.event.MouseEvent.BUTTON2
import java.awt.event.MouseEvent.BUTTON3
import java.awt.event.MouseEvent.BUTTON3_DOWN_MASK
import java.awt.event.MouseEvent.MOUSE_CLICKED
import java.awt.event.MouseEvent.MOUSE_DRAGGED
import java.awt.event.MouseEvent.MOUSE_ENTERED
import java.awt.event.MouseEvent.MOUSE_EXITED
import java.awt.event.MouseEvent.MOUSE_MOVED
import java.awt.event.MouseEvent.MOUSE_PRESSED
import java.awt.event.MouseEvent.MOUSE_RELEASED

internal fun PointerEvent.toAwt(target: Component): MouseEvent {
    val id = when (type) {
        Type.Up    -> MOUSE_RELEASED
        Type.Down  -> MOUSE_PRESSED
        Type.Move  -> MOUSE_MOVED
        Type.Exit  -> MOUSE_EXITED
        Type.Drag  -> MOUSE_DRAGGED
        Type.Click -> MOUSE_CLICKED
        Type.Enter -> MOUSE_ENTERED
    }

    val button = when (buttons) {
        setOf(SystemPointerEvent.Button.Button2) -> BUTTON2
        setOf(SystemPointerEvent.Button.Button3) -> BUTTON3
        else -> BUTTON1
    }

    // FIXME:
    val time = Clock.System.now().toEpochMilliseconds()

    var modifiers = when (button) {
        BUTTON2 -> BUTTON2_DOWN_MASK
        BUTTON3 -> BUTTON3_DOWN_MASK
        else    -> BUTTON1_DOWN_MASK
    }

    if (Alt in this.modifiers) {
        modifiers = modifiers or ALT_DOWN_MASK
    }

    if (Ctrl in this.modifiers) {
        modifiers = modifiers or CTRL_DOWN_MASK
    }

    if (Shift in this.modifiers) {
        modifiers = modifiers or SHIFT_DOWN_MASK
    }

    if (Meta in this.modifiers) {
        modifiers = modifiers or META_DOWN_MASK
    }

    return MouseEvent(target, id, time, modifiers, location.x.toInt(), location.y.toInt(), clickCount, false, button)
}
