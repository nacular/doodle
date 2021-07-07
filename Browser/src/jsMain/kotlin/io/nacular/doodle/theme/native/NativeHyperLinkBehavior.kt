package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.NativeHyperLinkFactory
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.PointerEvent

/**
 * Allows full control over how native [HyperLink]s are styled. The given behavior is delegated
 * to for all visual styling, but the browser will also treat the view as it does un-styled links.
 */
public interface NativeHyperLinkBehaviorBuilder {
    /**
     * Wraps [behavior] with other native styling for hyper links.
     *
     * @param behavior to be "wrapped"
     * @return a new Behavior for the link
     */
    public operator fun invoke(hyperLink: HyperLink, behavior: Behavior<HyperLink>): Behavior<HyperLink>
}

private class NativeHyperLinkBehaviorBuilderImpl(private val nativeHyperLinkFactory: NativeHyperLinkFactory):
        NativeHyperLinkBehaviorBuilder {
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
