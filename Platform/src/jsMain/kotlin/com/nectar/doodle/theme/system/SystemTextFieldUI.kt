package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.text.TextFieldRenderer
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeTextFieldFactory


class SystemTextFieldUI(nativeTextFieldFactory: NativeTextFieldFactory, textField: TextField): TextFieldRenderer {
    private val nativePeer by lazy { nativeTextFieldFactory(textField) }

    override fun fitTextSize(textField: TextField) = nativePeer.fitTextSize()

    override fun render(view: TextField, canvas: Canvas) {
        nativePeer.size = view.size

        nativePeer.render(canvas)
    }

    override fun uninstall(view: TextField) {
        super.uninstall(view)

        nativePeer.discard()
    }
}
