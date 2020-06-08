package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.panels.ScrollPanelBehavior
import io.nacular.doodle.drawing.impl.NativeScrollPanelFactory
import io.nacular.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 2/5/18.
 */
internal class NativeScrollPanelBehavior(nativeScrollPanelFactory: NativeScrollPanelFactory, scrollPanel: ScrollPanel): ScrollPanelBehavior {
    override fun scrollTo(point: Point) {
        nativePeer.scrollTo(point)
    }

    override var onScroll: ((Point) -> Unit)? = null

    private val nativePeer by lazy {
        nativeScrollPanelFactory(scrollPanel) {
            onScroll?.invoke(it)
        }
    }

    override fun install(view: ScrollPanel) {
        nativePeer // Load
    }

    override fun uninstall(view: ScrollPanel) {
        nativePeer.discard()
    }
}