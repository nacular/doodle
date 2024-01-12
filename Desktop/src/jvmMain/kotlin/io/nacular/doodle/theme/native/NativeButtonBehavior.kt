package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.theme.native.NativeTheme.WindowDiscovery
import kotlinx.coroutines.CoroutineScope
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.MOUSE_DRAGGED
import java.awt.event.MouseEvent.MOUSE_MOVED
import javax.swing.JButton
import kotlin.coroutines.CoroutineContext


internal class JButtonPeer(focusManager: FocusManager?, button: Button): JButton(), AbstractNativeButtonBehavior.Peer {
    private val button: Button? = button

    override var ignoreSelectionChange = false
    override var selected_ get() = isSelected; set(value) { isSelected = value }

    init {
        text                      = button.text
        isSelected                = button.selected
        focusTraversalKeysEnabled = false

        addFocusListener(object: FocusListener {
            override fun focusGained(e: FocusEvent?) { focusManager?.requestFocus(button) }
            override fun focusLost  (e: FocusEvent?) {
                if (button == focusManager?.focusOwner) { focusManager.clearFocus() }
            }
        })

        addActionListener { if (!ignoreSelectionChange) button.click() }
    }

    override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
        button?.rerender()
    }

    override fun handleMouseEvent(e: MouseEvent?) = when (e?.id) {
        MOUSE_MOVED, MOUSE_DRAGGED -> processMouseMotionEvent(e)
        else                       -> processMouseEvent      (e)
    }
}

internal class NativeButtonBehavior(
    window                   : WindowDiscovery,
    appScope                 : CoroutineScope,
    uiDispatcher             : CoroutineContext,
    swingGraphicsFactory     : SwingGraphicsFactory,
    textMetrics              : TextMetrics,
    swingFocusManager        : javax.swing.FocusManager,
    focusManager             : FocusManager?,
    nativePointerPreprocessor: NativePointerPreprocessor?
): AbstractNativeButtonBehavior<Button, JButtonPeer>(window, appScope, uiDispatcher, textMetrics, swingGraphicsFactory, swingFocusManager, focusManager, nativePointerPreprocessor) {
    override fun createPeer (button: Button) = JButtonPeer(focusManager, button)
}