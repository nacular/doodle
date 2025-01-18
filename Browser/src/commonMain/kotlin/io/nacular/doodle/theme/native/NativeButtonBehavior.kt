package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.View.Companion.fixed
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.NativeButtonFactory
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default

internal class NativeButtonBehavior(
    nativeButtonFactory: NativeButtonFactory,
    textMetrics        : TextMetrics,
    focusManager       : FocusManager?,
    button             : Button): CommonTextButtonBehavior<Button>(textMetrics, focusManager = focusManager) {

    private val nativePeer       = nativeButtonFactory(button)
    private var oldCursor        : Cursor? = null
    private var oldPreferredSize = button.preferredSize

    override fun render(view: Button, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun mirrorWhenRightToLeft(view: Button) = false

    override fun install(view: Button) {
        super.install(view)

        view.apply {
            cursor        = Default
            preferredSize = fixed(nativePeer.idealSize)
        }
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

        nativePeer.discard()

        view.apply {
            cursor        = oldCursor
            preferredSize = oldPreferredSize
        }
    }

    override fun released(event: PointerEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons.isEmpty()) {
            model.pressed = false
            model.armed   = false
        }
    }

    override fun pointerChanged(button: Button) {}

    override fun released(event: KeyEvent) { /* intentional no-op */ }

    override fun pressed(event: KeyEvent) { /* intentional no-op */ }
}
