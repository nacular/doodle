package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.panels.ScrollPanelBehavior
import com.nectar.doodle.drawing.impl.NativeScrollPanelFactory
import com.nectar.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 2/5/18.
 */
class SystemScrollPanelBehavior(nativeScrollPanelFactory: NativeScrollPanelFactory, scrollPanel: ScrollPanel): ScrollPanelBehavior {
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