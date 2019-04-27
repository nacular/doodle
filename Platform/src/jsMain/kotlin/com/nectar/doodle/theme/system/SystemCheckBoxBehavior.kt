package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.theme.AbstractTextButtonBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.NativeCheckBoxRadioButtonFactory
import com.nectar.doodle.drawing.impl.Type.Check
import com.nectar.doodle.system.Cursor

/**
 * Created by Nicholas Eddy on 4/26/19.
 */
internal class SystemCheckBoxBehavior(
        private val nativeCheckBoxRadioButtonFactory: NativeCheckBoxRadioButtonFactory,
                    textMetrics                     : TextMetrics,
        private val button                          : CheckBox): AbstractTextButtonBehavior(textMetrics) {

    private val nativePeer by lazy { nativeCheckBoxRadioButtonFactory(button, Check) }

    override fun render(view: Button, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: Button) {
        super.install(view)

        view.cursor = Cursor.Default

        view.idealSize = nativePeer.idealSize

        view.idealSize?.let { view.size = it }

        view.rerender()
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

        nativePeer.discard()

        view.cursor = null
    }
}
