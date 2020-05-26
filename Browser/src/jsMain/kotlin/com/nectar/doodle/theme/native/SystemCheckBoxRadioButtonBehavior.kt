package com.nectar.doodle.theme.native

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.ToggleButton
import com.nectar.doodle.controls.theme.AbstractTextButtonBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.NativeCheckBoxRadioButtonFactory
import com.nectar.doodle.drawing.impl.Type
import com.nectar.doodle.drawing.impl.Type.Check
import com.nectar.doodle.drawing.impl.Type.Radio
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.system.Cursor

/**
 * Created by Nicholas Eddy on 4/26/19.
 */
internal abstract class AbstractSystemCheckBoxRadioButtonBehavior(
        private val nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
                    textMetrics                     : TextMetrics,
        private val button                          : Button,
        private val type                            : Type): AbstractTextButtonBehavior<ToggleButton>(textMetrics) {

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

internal class SystemCheckBoxBehavior(
        nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
        textMetrics                     : TextMetrics,
        button                          : Button): AbstractSystemCheckBoxRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, button, Check)


internal class SystemRadioButtonBehavior(
        nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
        textMetrics                     : TextMetrics,
        button                          : Button): AbstractSystemCheckBoxRadioButtonBehavior(nativeCheckBoxRadioButtonFactory, textMetrics, button, Radio)