package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.dom.Overflow.Hidden
import com.nectar.doodle.dom.Overflow.Scroll
import com.nectar.doodle.dom.setOverflow
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 2/5/18.
 */

interface NativeScrollPanelFactory {
    operator fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit): NativeScrollPanel
}

class NativeScrollPanelFactoryImpl internal constructor(
        private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val handlerFactory: NativeEventHandlerFactory): NativeScrollPanelFactory {
    override fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit) = NativeScrollPanel(
            handlerFactory,
            graphicsDevice,
            scrollPanel,
            onScroll)
}

class NativeScrollPanel internal constructor(
                    handlerFactory: NativeEventHandlerFactory,
                    graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val panel         : ScrollPanel,
        private val scrolled      : (Point) -> Unit): NativeEventListener {

    private val eventHandler: NativeEventHandler
    private val rootElement = graphicsDevice[panel].rootElement

    init {
        rootElement.apply {
            style.setOverflow(Scroll)

            panel.apply {
                scroll(scroll.x, scroll.y)
            }
        }

        eventHandler = handlerFactory(rootElement, this).apply {
            registerScrollListener()
        }
    }

    fun discard() {
        rootElement.also {
            it.style.setOverflow(Hidden)

            eventHandler.unregisterScrollListener()
        }
    }

    override fun onScroll() = true.also {
        scrolled(Point(x = rootElement.scrollLeft, y = rootElement.scrollTop))
    }

    fun scrollTo(point: Point) {
        rootElement.scrollTo(point.x, point.y)
    }
}