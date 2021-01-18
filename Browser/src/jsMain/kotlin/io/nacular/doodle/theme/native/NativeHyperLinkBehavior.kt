package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.NativeHyperLinkFactory
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.PointerEvent

internal class NativeHyperLinkBehavior(nativeHyperLinkFactory: NativeHyperLinkFactory, textMetrics: TextMetrics, hyperLink: HyperLink): CommonTextButtonBehavior<HyperLink>(textMetrics) {

    private val nativePeer by lazy{ nativeHyperLinkFactory(hyperLink) }

    override fun render(view: HyperLink, canvas: Canvas) {
        nativePeer.render(canvas)
    }

    override fun install(view: HyperLink) {
        super.install(view)

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

    override fun released(event: PointerEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons.isEmpty()) {
            model.pressed = false
            model.armed   = false
        }
    }

    override fun pointerChanged(button: HyperLink) {}

    override fun released(event: KeyEvent) {}

    override fun pressed(event: KeyEvent) {}
}
