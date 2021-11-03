package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.NativeHyperLinkFactory
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.PointerEvent

@Deprecated("Replace with `NativeHyperLinkStyler`", replaceWith = ReplaceWith("NativeHyperLinkStyler"))
public typealias NativeHyperLinkBehaviorBuilder = NativeHyperLinkStyler

internal class NativeHyperLinkStylerImpl(private val nativeHyperLinkFactory: NativeHyperLinkFactory):
        NativeHyperLinkStyler {
    override fun invoke(hyperLink: HyperLink, behavior: Behavior<HyperLink>): Behavior<HyperLink> = NativeHyperLinkBehaviorWrapper(
            nativeHyperLinkFactory,
            hyperLink,
            behavior
    )
}

private class NativeHyperLinkBehaviorWrapper(
        nativeHyperLinkFactory: NativeHyperLinkFactory,
        hyperLink             : HyperLink,
        private val delegate  : Behavior<HyperLink>): Behavior<HyperLink> by delegate {

    private val nativePeer by lazy {
        nativeHyperLinkFactory(hyperLink) { link, canvas ->
            delegate.render(link, canvas)
        }
    }

    override fun render(view: HyperLink, canvas: Canvas) {
        nativePeer.render(canvas)
    }
}

internal class NativeHyperLinkBehavior(
                    nativeHyperLinkFactory: NativeHyperLinkFactory,
                    textMetrics           : TextMetrics,
                    hyperLink             : HyperLink,
        private val customRenderer        : (CommonTextButtonBehavior<HyperLink>.(HyperLink, Canvas) -> Unit)? = null
): CommonTextButtonBehavior<HyperLink>(textMetrics) {

    private val nativePeer by lazy {
        val renderer: ((HyperLink, Canvas) -> Unit)? = customRenderer?.let { renderer ->
            { link, canvas ->
                renderer(this, link, canvas)
            }
        }

        nativeHyperLinkFactory(hyperLink, renderer)
    }

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
        val button = event.source as HyperLink
        val model  = button.model

        if (button.enabled && event.buttons.isEmpty()) {
            model.pressed = false
            model.armed   = false

            pointerChanged(button)
        }
    }

    override fun pointerChanged(button: HyperLink) {
        if (customRenderer != null) {
            super.pointerChanged(button)
        }
    }

    override fun released(event: KeyEvent) {}

    override fun pressed(event: KeyEvent) {}
}
