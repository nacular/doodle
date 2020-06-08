package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.NativeCheckBoxRadioButtonFactory
import io.nacular.doodle.drawing.impl.Type
import io.nacular.doodle.drawing.impl.Type.Check
import io.nacular.doodle.drawing.impl.Type.Radio
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.system.Cursor

/**
 * Created by Nicholas Eddy on 4/26/19.
 */
internal abstract class CommonNativeCheckBoxRadioButtonBehavior(
        private val nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
                    textMetrics                     : TextMetrics,
        private val button                          : Button,
        private val type                            : Type): CommonTextButtonBehavior<ToggleButton>(textMetrics) {

    private val nativePeer by lazy { nativeCheckBoxRadioButtonFactory(button, type) }

    override fun render(view: ToggleButton, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: ToggleButton) {
        super.install(view)

        view.cursor    = Cursor.Default
        view.idealSize = nativePeer.idealSize
        view.idealSize?.let { view.size = it }

        view.rerender()
    }

    override fun uninstall(view: ToggleButton) {
        super.uninstall(view)

        nativePeer.discard()

        view.cursor = null
    }

    override fun keyReleased(event: KeyEvent) {}

    override fun keyPressed(event: KeyEvent) {}
}

internal class NativeCheckBoxBehavior(
        nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
        textMetrics                     : TextMetrics,
        button                          : Button): CommonNativeCheckBoxRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, button, Check)


internal class NativeRadioButtonBehavior(
        nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
        textMetrics                     : TextMetrics,
        button                          : Button): CommonNativeCheckBoxRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, button, Radio)