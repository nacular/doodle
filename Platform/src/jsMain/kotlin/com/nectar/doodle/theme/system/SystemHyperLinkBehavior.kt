package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.HyperLink
import com.nectar.doodle.controls.theme.AbstractTextButtonBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.drawing.impl.NativeHyperLinkFactory
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.system.Cursor.Companion.Default

internal class SystemHyperLinkBehavior(nativeHyperLinkFactory: NativeHyperLinkFactory, textMetrics: TextMetrics, hyperLink: HyperLink): AbstractTextButtonBehavior<HyperLink>(textMetrics) {

    private val nativePeer by lazy{ nativeHyperLinkFactory(hyperLink) }

    override fun render(view: HyperLink, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: HyperLink) {
        super.install(view)

        view.cursor    = Default
        view.idealSize = nativePeer.idealSize

        view.idealSize?.let {
            view.size = it
        }

        view.rerender()
    }

    override fun uninstall(view: HyperLink) {
        super.uninstall(view)

        nativePeer.discard()

        view.cursor = null
    }

    override fun mouseReleased(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons.isEmpty()) {
            model.pressed = false
            model.armed   = false
        }
    }

    override fun mouseChanged(button: HyperLink) {}

    override fun keyReleased(event: KeyEvent) {}

    override fun keyPressed(event: KeyEvent) {}
}
