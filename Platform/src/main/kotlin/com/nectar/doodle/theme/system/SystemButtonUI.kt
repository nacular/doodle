package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeButtonFactory
import com.nectar.doodle.system.Cursor

internal class SystemButtonUI(nativeButtonFactory: NativeButtonFactory, button: Button): AbstractSystemButtonUI(button) {

    private val nativePeer by lazy{ nativeButtonFactory(button) }

    override fun render(canvas: Canvas, gizmo: Button) {
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
}
