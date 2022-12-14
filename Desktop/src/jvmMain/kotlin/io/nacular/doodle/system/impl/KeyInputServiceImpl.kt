package io.nacular.doodle.system.impl

import io.nacular.doodle.event.KeyCode
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.event.KeyState.Type
import io.nacular.doodle.event.KeyState.Type.Down
import io.nacular.doodle.event.KeyState.Type.Up
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.system.KeyInputService
import io.nacular.doodle.system.KeyInputService.KeyResponse.Consumed
import io.nacular.doodle.system.KeyInputService.Listener
import io.nacular.doodle.system.KeyInputService.Postprocessor
import io.nacular.doodle.system.KeyInputService.Preprocessor
import io.nacular.doodle.system.SystemInputEvent
import io.nacular.doodle.utils.ifTrue
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.KEY_PRESSED
import java.awt.event.KeyEvent.VK_ALT
import java.awt.event.KeyEvent.VK_ALT_GRAPH
import java.awt.event.KeyEvent.VK_BACK_SPACE
import java.awt.event.KeyEvent.VK_CAPS_LOCK
import java.awt.event.KeyEvent.VK_CONTROL
import java.awt.event.KeyEvent.VK_DELETE
import java.awt.event.KeyEvent.VK_DOWN
import java.awt.event.KeyEvent.VK_END
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_HOME
import java.awt.event.KeyEvent.VK_LEFT
import java.awt.event.KeyEvent.VK_META
import java.awt.event.KeyEvent.VK_NUM_LOCK
import java.awt.event.KeyEvent.VK_PAGE_DOWN
import java.awt.event.KeyEvent.VK_PAGE_UP
import java.awt.event.KeyEvent.VK_RIGHT
import java.awt.event.KeyEvent.VK_SCROLL_LOCK
import java.awt.event.KeyEvent.VK_SHIFT
import java.awt.event.KeyEvent.VK_TAB
import java.awt.event.KeyEvent.VK_UP


/**
 * Created by Nicholas Eddy on 3/10/18.
 */
internal class KeyInputServiceImpl(private val keyboardFocusManager: KeyboardFocusManager): KeyInputService, KeyEventDispatcher, KeyAdapter() {

    private var started        = false
    private var listeners      = mutableSetOf<Listener>     ()
    private var preprocessors  = mutableSetOf<Preprocessor> ()
    private var postprocessors = mutableSetOf<Postprocessor>()

    override fun plusAssign(listener: Listener) {
        if (!started) {
            startUp()
        }

        listeners.add(listener)
    }

    override fun minusAssign(listener: Listener) {
        listeners.remove(listener)

        if (unused) {
            shutdown()
        }
    }

    override fun plusAssign(processor: Preprocessor) {
        if (!started) {
            startUp()
        }

        preprocessors.add(processor)
    }

    override fun minusAssign(processor: Preprocessor) {
        preprocessors.remove(processor)

        if (unused) {
            shutdown()
        }
    }

    override fun plusAssign(processor: Postprocessor) {
        if (!started) {
            startUp()
        }

        postprocessors.add(processor)
    }

    override fun minusAssign(processor: Postprocessor) {
        postprocessors.remove(processor)

        if (unused) {
            shutdown()
        }
    }

    // FIXME: Only notify UP once?
    override fun dispatchKeyEvent(e: KeyEvent) = false.also {
        notifyKeyEvent(e, when (e.id) {
            KEY_PRESSED -> Down
            else        -> Up
        })
    }

    private fun createModifiers(e: KeyEvent) = mutableSetOf<SystemInputEvent.Modifier>().also {
        e.isAltDown.ifTrue     { it += SystemInputEvent.Modifier.Alt   }
        e.isMetaDown.ifTrue    { it += SystemInputEvent.Modifier.Meta  }
        e.isShiftDown.ifTrue   { it += SystemInputEvent.Modifier.Shift }
        e.isControlDown.ifTrue { it += SystemInputEvent.Modifier.Ctrl  }
    }

    private val unused: Boolean get() = listeners.isEmpty() && preprocessors.isEmpty() && postprocessors.isEmpty()

    private fun notifyKeyEvent(e: KeyEvent, type: Type) {
        val keyState = KeyState(e.doodleKeyCode, e.doodleKeyText, createModifiers(e), type)

        preprocessors.forEach {
            if (it(keyState) == Consumed) {
                return
            }
        }

        listeners.forEach {
            if (it(keyState) == Consumed) {
                return
            }
        }

        postprocessors.forEach {
            if (it(keyState) == Consumed) {
                return
            }
        }
    }

    private val KeyEvent.doodleKeyCode: KeyCode get() = when (keyCode) {
        VK_BACK_SPACE -> KeyCode.Backspace
        VK_TAB        -> KeyCode.Tab
        VK_ENTER      -> KeyCode.Enter
        VK_ESCAPE     -> KeyCode.Escape
        VK_DELETE     -> KeyCode.Delete

        VK_ALT -> KeyCode.AltLeft
        VK_ALT_GRAPH -> KeyCode.AltRight
        VK_CAPS_LOCK -> KeyCode.CapsLock
        VK_CONTROL -> KeyCode.ControlLeft
//            VK_FN
//            VK_FNLOCK
        VK_META -> KeyCode.MetaLeft
        VK_NUM_LOCK -> KeyCode.NumLock
        VK_SCROLL_LOCK -> KeyCode.ScrollLock
        VK_SHIFT -> KeyCode.ShiftLeft
//            VK_SYMBOL
//            VK_SYMBOLLOCK ->
        VK_DOWN -> KeyCode.ArrowDown
        VK_LEFT -> KeyCode.ArrowLeft
        VK_RIGHT -> KeyCode.ArrowRight
        VK_UP -> KeyCode.ArrowUp
        VK_END -> KeyCode.End
        VK_HOME -> KeyCode.Home
        VK_PAGE_DOWN -> KeyCode.PageDown
        VK_PAGE_UP -> KeyCode.PageUp
        else           -> KeyCode("") // FIXME
    }

    private val KeyEvent.doodleKeyText: KeyText get() = when (keyCode) {
        VK_BACK_SPACE -> KeyText.Backspace
        VK_TAB -> KeyText.Tab
        VK_ENTER -> KeyText.Enter
        VK_ESCAPE -> KeyText.Escape
        VK_DELETE -> KeyText.Delete

        VK_ALT -> KeyText.Alt
        VK_ALT_GRAPH -> KeyText.AltGraph
        VK_CAPS_LOCK -> KeyText.CapsLock
        VK_CONTROL -> KeyText.Control
//            VK_FN
//            VK_FNLOCK
        VK_META -> KeyText.Meta
        VK_NUM_LOCK -> KeyText.NumLock
        VK_SCROLL_LOCK -> KeyText.ScrollLock
        VK_SHIFT -> KeyText.Shift
//            VK_SYMBOL
//            VK_SYMBOLLOCK ->
        VK_DOWN -> KeyText.ArrowDown
        VK_LEFT -> KeyText.ArrowLeft
        VK_RIGHT -> KeyText.ArrowRight
        VK_UP -> KeyText.ArrowUp
        VK_END -> KeyText.End
        VK_HOME -> KeyText.Home
        VK_PAGE_DOWN -> KeyText.PageDown
        VK_PAGE_UP -> KeyText.PageUp
        else           -> KeyText(keyChar.toString())
    }

    private fun startUp() {
        if (!started) {
            keyboardFocusManager.addKeyEventDispatcher(this)
            started = true
        }
    }

    private fun shutdown() {
        if (started) {
            keyboardFocusManager.removeKeyEventDispatcher(this)
            started = false
        }
    }
}
