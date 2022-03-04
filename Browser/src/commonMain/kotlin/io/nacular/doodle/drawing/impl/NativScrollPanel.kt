package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Hidden
import io.nacular.doodle.dom.Overflow.Scroll
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.scrollTo
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scrollBehavior
import io.nacular.doodle.willChange

/**
 * Created by Nicholas Eddy on 2/5/18.
 */

internal interface NativeScrollPanelFactory {
    operator fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit): NativeScrollPanel
}

internal class NativeScrollPanelFactoryImpl internal constructor(
        private val smoothScroll  : Boolean = false,
        private val scheduler     : Scheduler,
        private val htmlFactory   : HtmlFactory,
        private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val handlerFactory: NativeEventHandlerFactory): NativeScrollPanelFactory {
    override fun invoke(scrollPanel: ScrollPanel, onScroll: (Point) -> Unit) = NativeScrollPanel(
        scheduler,
        htmlFactory,
        handlerFactory,
        graphicsDevice,
        scrollPanel,
        smoothScroll,
        onScroll
    )
}

internal class NativeScrollPanel internal constructor(
        private val scheduler     : Scheduler,
                    htmlFactory   : HtmlFactory,
                    handlerFactory: NativeEventHandlerFactory,
                    graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val panel         : ScrollPanel,
        private val smoothScroll  : Boolean,
        private val scrolled      : (Point) -> Unit): NativeEventListener {

    private val eventHandler: NativeEventHandler
    private val rootElement = graphicsDevice[panel].rootElement

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _,old,new ->
        if (new.size != old.size) {
            (spacerDiv.firstChild as? HTMLElement)?.style?.setSize(new.size)
        }
    }

    private val contentBoundsChanged: (View, Rectangle, Rectangle) -> Unit = { view, old, new ->
        if (new.size != old.size) {
            spacerDiv.style.setSize(view.size)
        }
    }

    private val contentChanged: (ScrollPanel, View?, View?) -> Unit = { _,old,new ->
        old?.let { view ->
            graphicsDevice[view].let {
                it.removedFromNativeScroll()
                rootElement.remove(spacerDiv)

                view.boundsChanged  -= contentBoundsChanged
                panel.boundsChanged -= boundsChanged
            }
        }

        new?.let { view ->
            graphicsDevice[view].let {
                it.addedToNativeScroll()
                it.rootElement.style.willChange = "transform"

                rootElement.add(spacerDiv)

                spacerDiv.add(it.rootElement.parent!!)
                (spacerDiv.firstChild as? HTMLElement)?.style?.setSize(panel.size)

                spacerDiv.style.setSize(view.size)

                view.boundsChanged  += contentBoundsChanged
                panel.boundsChanged += boundsChanged
            }
        }
    }

    private val spacerDiv: HTMLElement by lazy {
        htmlFactory.create<HTMLElement>("DIV").apply {
            style.overflowX = "unset"
            style.overflowY = "unset"
        }
    }

    init {
        rootElement.apply {
            style.setOverflow(Scroll())

            if (smoothScroll) {
                style.scrollBehavior = "smooth"
            }

            scrollTo(panel.scroll)
        }

        eventHandler = handlerFactory(rootElement, this).apply {
            registerScrollListener()
        }

        panel.contentChanged += contentChanged

        contentChanged(panel, null, panel.content)
    }

    fun discard() {
        panel.boundsChanged  -= boundsChanged
        panel.contentChanged -= contentChanged

        rootElement.apply {
            style.setOverflow(Hidden())

            eventHandler.unregisterScrollListener()
        }
    }

    private var scroll = Origin

    override fun onScroll(event: Event) = true.also {
        scroll = Point(x = rootElement.scrollLeft, y = rootElement.scrollTop)

        scrolled(scroll)
    }

    fun scrollTo(point: Point) {
        // Defer scroll since it is possible element sizes have not been updated to match
        // ScrollPanel size
        scheduler.now {
            rootElement.scrollTo(point)

            // Handle case that requested scroll was not possible (say if rootElement's child is not larger than it)
            if (scroll != point) {
                scrolled(scroll)
            }
        }
    }
}