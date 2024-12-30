package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.panels.ScrollPanelBehavior
import io.nacular.doodle.controls.panels.ScrollPanelBehavior.ScrollBarType
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeScrollPanel
import io.nacular.doodle.drawing.impl.NativeScrollPanelFactory
import io.nacular.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 2/5/18.
 */
internal class NativeScrollPanelBehavior(
    private val nativeScrollPanelFactory: NativeScrollPanelFactory,
    private val managedScrolling        : Boolean,
): ScrollPanelBehavior {
    private var hasRendered     = false
    private var pendingScrollTo = null as Point?

    override fun scrollTo(panel: ScrollPanel, point: Point) {
        when {
            hasRendered -> nativePeer.scrollTo(point)
            else        -> pendingScrollTo = point
        }
    }

    override var onScroll: ((Point) -> Unit)? = null

    override var scrollBarSizeChanged: ((ScrollBarType, Double) -> Unit)? = null

    private lateinit var nativePeer: NativeScrollPanel

    override fun render(view: ScrollPanel, canvas: Canvas) {
        // Load on first render to avoid premature creation of Graphics Surface, which would
        // mess up element ordering.
        nativePeer

        hasRendered = true

        pendingScrollTo?.let {
            scrollTo(view, it)
            pendingScrollTo = null
        }
    }

    override fun install(view: ScrollPanel) {
        super.install(view)

        nativePeer = nativeScrollPanelFactory(view, managedScrolling, barChanged = { type, size ->   scrollBarSizeChanged?.invoke(type, size) }) {
            onScroll?.invoke(it)
        }

        scrollTo(view, view.scroll)
    }

    override fun uninstall(view: ScrollPanel) {
        nativePeer.discard()
    }
}