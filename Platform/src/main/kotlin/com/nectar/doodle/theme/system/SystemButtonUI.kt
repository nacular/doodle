package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.theme.AbstractButtonUI
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.NativeButtonFactory
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemMouseEvent

internal class SystemButtonUI(nativeButtonFactory: NativeButtonFactory, textMetrics: TextMetrics, button: Button): AbstractButtonUI(textMetrics) {

    private val nativePeer by lazy{ nativeButtonFactory(button) }

    override fun render(gizmo: Button, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(gizmo: Button) {
        super.install(gizmo)

        gizmo.cursor = Cursor.Default

//        if (shouldOverwriteProperty(gizmo.getIdealSize())) {
            gizmo.idealSize = nativePeer.idealSize
//        }

        gizmo.idealSize?.let {
            gizmo.size = it
        }

//        if (gizmo.idealSize != null /*&&
//            ( aButton.getParent() == null || aButton.getParent().getLayout() == null )*/) {
//            gizmo.size = gizmo.idealSize
//        }
    }

    override fun uninstall(gizmo: Button) {
        super.uninstall(gizmo)

        nativePeer.discard()

        gizmo.cursor = null
    }

    override fun mouseReleased(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons == setOf(SystemMouseEvent.Button.Button1)) {
            model.armed   = false
            model.pressed = false
        }
    }

    override fun mouseChanged(button: Button) {}
}
