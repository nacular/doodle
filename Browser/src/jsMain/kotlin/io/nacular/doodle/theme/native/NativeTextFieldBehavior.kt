package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFieldBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeTextFieldFactory

internal class NativeTextFieldStylerImpl(private val nativeTextFieldFactory: NativeTextFieldFactory):
        NativeTextFieldStyler {
    override fun invoke(textField: TextField, behavior: NativeTextFieldBehaviorModifier): TextFieldBehavior = NativeTextFieldBehaviorWrapper(
            nativeTextFieldFactory,
            textField,
            behavior
    )
}

private class NativeTextFieldBehaviorWrapper(
        nativeTextFieldFactory: NativeTextFieldFactory,
        textField             : TextField,
        private val delegate  : NativeTextFieldBehaviorModifier): TextFieldBehavior, Behavior<TextField> by delegate {
    private val nativePeer by lazy {
        nativeTextFieldFactory(textField)
    }

    override fun fitTextSize(textField: TextField) = nativePeer.fitTextSize()

    override fun render(view: TextField, canvas: Canvas) {
        delegate.renderBackground(view, canvas)
        nativePeer.render(canvas)
        delegate.renderForeground(view, canvas)
    }

    override fun uninstall(view: TextField) {
        super<TextFieldBehavior>.uninstall(view)

        nativePeer.discard()
    }
}

internal class NativeTextFieldBehavior(nativeTextFieldFactory: NativeTextFieldFactory, textField: TextField): TextFieldBehavior {
    private val nativePeer by lazy { nativeTextFieldFactory(textField) }

    override fun fitTextSize       (textField: TextField) = nativePeer.fitTextSize()
    override fun clipCanvasToBounds(view     : TextField) = nativePeer.clipCanvasToBounds

    override fun render(view: TextField, canvas: Canvas) {
        nativePeer.size = view.size

        nativePeer.render(canvas)
    }

    override fun uninstall(view: TextField) {
        super.uninstall(view)

        nativePeer.discard()
    }
}
