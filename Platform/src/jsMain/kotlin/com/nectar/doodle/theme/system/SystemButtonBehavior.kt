package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.theme.AbstractTextButtonBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.NativeButtonFactory
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.system.Cursor.Companion.Default

internal class SystemButtonBehavior(nativeButtonFactory: NativeButtonFactory, textMetrics: TextMetrics, button: Button): AbstractTextButtonBehavior<Button>(textMetrics) {

    private val nativePeer by lazy { nativeButtonFactory(button) }

    override fun render(view: Button, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: Button) {
        super.install(view)

        view.cursor    = Default
        view.idealSize = nativePeer.idealSize

//        view.idealSize?.let {
//            view.size = it
//        }

        view.rerender()
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

        nativePeer.discard()

        view.cursor = null
    }

    override fun mouseReleased(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons.isEmpty()) {
            model.pressed = false
            model.armed   = false
        }
    }

    override fun mouseChanged(button: Button) {}

    override fun keyReleased(event: KeyEvent) {}

    override fun keyPressed(event: KeyEvent) {}
}
