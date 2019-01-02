package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Overflow.Hidden
import com.nectar.doodle.dom.Overflow.Scroll
import com.nectar.doodle.dom.Position
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.setOverflow
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import org.w3c.dom.HTMLElement

/**
 * Created by Nicholas Eddy on 2/5/18.
 */

interface NativeScrollPanelFactory {
    operator fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit): NativeScrollPanel
}

class NativeScrollPanelFactoryImpl internal constructor(
        private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val handlerFactory: NativeEventHandlerFactory,
        private val htmlFactory   : HtmlFactory): NativeScrollPanelFactory {
    override fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit) = NativeScrollPanel(handlerFactory, graphicsDevice, htmlFactory, scrollPanel, onScroll)
}

class NativeScrollPanel internal constructor(
                    handlerFactory: NativeEventHandlerFactory,
                    graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
                    htmlFactory   : HtmlFactory,
        private val panel         : ScrollPanel,
        private val scrolled      : (Point) -> Unit): NativeEventListener {

    private val eventHandler: NativeEventHandler
    private val rootElement   = graphicsDevice[panel].rootElement
    private val backPane      = htmlFactory.create<HTMLElement>()
    private val boundsChanged_ = ::boundsChanged

    init {
        rootElement.apply {
            style.setOverflow(Scroll)

            panel.apply {
                scroll(scroll.x, scroll.y)
            }

            /*
             * Back pane is used to work-around Safari, which has jitter when synchronizing bounds of content with other view's bounds
             */
            backPane.style.setPosition(Position.Absolute)

            insert(backPane, 0)
        }

        panel.content?.let { it.boundsChanged += boundsChanged_ }

        panel.contentChanged += { _,old,new ->
            old?.let { it.boundsChanged -= boundsChanged_ }
            new?.let { it.boundsChanged += boundsChanged_ }
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

    @Suppress("UNUSED_PARAMETER")
    private fun boundsChanged(view: View, old: Rectangle, new: Rectangle) {
        backPane.style.setSize(new.size)
    }
}