package com.nectar.doodle.theme.native

import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.text.TextFieldBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeTextFieldFactory


internal class SystemTextFieldBehavior(nativeTextFieldFactory: NativeTextFieldFactory, textField: TextField): TextFieldBehavior {
    private val nativePeer by lazy { nativeTextFieldFactory(textField) }

    override fun fitTextSize(textField: TextField) = nativePeer.fitTextSize()

    override fun render(view: TextField, canvas: Canvas) {
        nativePeer.size = view.size

        nativePeer.render(canvas)
    }

    override fun install(view: TextField) {
        view.rerender()
    }

    override fun uninstall(view: TextField) {
        super.uninstall(view)

        nativePeer.discard()
    }
}
