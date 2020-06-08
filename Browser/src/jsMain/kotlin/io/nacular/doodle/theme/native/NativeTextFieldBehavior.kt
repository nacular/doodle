package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFieldBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeTextFieldFactory


internal class NativeTextFieldBehavior(nativeTextFieldFactory: NativeTextFieldFactory, textField: TextField): TextFieldBehavior {
    private val nativePeer by lazy { nativeTextFieldFactory(textField) }

    override fun fitTextSize(textField: TextField) = nativePeer.fitTextSize()
    override fun clipCanvasToBounds(view: TextField) = nativePeer.clipCanvasToBounds

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
