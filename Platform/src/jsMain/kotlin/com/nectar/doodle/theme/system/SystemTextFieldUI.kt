package com.nectar.doodle.theme.system

import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeTextFieldFactory
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.controls.text.TextField


class SystemTextFieldUI(nativeTextFieldFactory: NativeTextFieldFactory, textField: TextField): Renderer<TextField> {

    private val nativePeer by lazy { nativeTextFieldFactory(textField) }

    override fun render(view: TextField, canvas: Canvas) {
        nativePeer.size = view.size

        nativePeer.render(canvas)
    }

    override fun uninstall(view: TextField) {
        super.uninstall(view)

        nativePeer.discard()
    }
}
