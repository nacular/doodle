package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.panels.ScrollPanelRenderer
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeScrollPanelFactory
import com.nectar.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 2/5/18.
 */
class SystemScrollPanelUI(nativeScrollPanelFactory: NativeScrollPanelFactory, scrollPanel: ScrollPanel): ScrollPanelRenderer {
    override fun scrollTo(point: Point) {
        nativePeer.scrollTo(point)
    }

    override var onScroll: ((Point) -> Unit)? = null

    private val nativePeer by lazy { nativeScrollPanelFactory(scrollPanel) {
        onScroll?.invoke(it)
    } }

    override fun render(canvas: Canvas, gizmo: ScrollPanel) {
        nativePeer
        // no-op
    }

    override fun uninstall(gizmo: ScrollPanel) {
        nativePeer.discard()
    }
}