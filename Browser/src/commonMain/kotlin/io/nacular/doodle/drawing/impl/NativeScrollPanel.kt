package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.ResizeObserver
import io.nacular.doodle.ResizeObserverInit
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.panels.ScrollPanelBehavior.ScrollBarType
import io.nacular.doodle.controls.panels.ScrollPanelBehavior.ScrollBarType.Horizontal
import io.nacular.doodle.controls.panels.ScrollPanelBehavior.ScrollBarType.Vertical
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Auto
import io.nacular.doodle.dom.Overflow.Hidden
import io.nacular.doodle.dom.Overflow.Scroll
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.height
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.scrollTo
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.width
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scrollBehavior
import io.nacular.doodle.willChange
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 2/5/18.
 */
internal interface NativeScrollPanelFactory {
    operator fun invoke(scrollPanel: ScrollPanel, barChanged: (ScrollBarType, Double) -> Unit, onScroll: (Point) -> Unit): NativeScrollPanel
}

internal class NativeScrollPanelFactoryImpl internal constructor(
        private val smoothScroll  : Boolean = false,
        private val scheduler     : Scheduler,
        private val htmlFactory   : HtmlFactory,
        private val graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val handlerFactory: NativeEventHandlerFactory): NativeScrollPanelFactory {
    override fun invoke(scrollPanel: ScrollPanel, barChanged: (ScrollBarType, Double) -> Unit, onScroll: (Point) -> Unit) = NativeScrollPanel(
        scheduler,
        htmlFactory,
        handlerFactory,
        graphicsDevice,
        scrollBarWidth,
        scrollPanel,
        smoothScroll,
        barChanged,
        onScroll
    )

    private val scrollBarWidth: Double by lazy {
        val outer = htmlFactory.create<HTMLElement>("DIV")

        outer.style.setOverflow(Scroll())

        htmlFactory.root.add(outer)

        val inner = htmlFactory.create<HTMLElement>("DIV")

        outer.appendChild(inner)

        val scrollbarWidth = outer.offsetWidth - inner.offsetWidth

        outer.parentNode?.removeChild(outer)

        scrollbarWidth.toDouble()
    }
}

internal class NativeScrollPanel internal constructor(
        private val scheduler     : Scheduler,
                    htmlFactory   : HtmlFactory,
                    handlerFactory: NativeEventHandlerFactory,
                    graphicsDevice: GraphicsDevice<RealGraphicsSurface>,
        private val scrollBarSize : Double,
        private val panel         : ScrollPanel,
        private val smoothScroll  : Boolean,
        private val barChanged    : (ScrollBarType, Double) -> Unit,
        private val scrolled      : (Point) -> Unit): NativeEventListener {

    private val eventHandler: NativeEventHandler
    private val rootElement = graphicsDevice[panel].rootElement.apply {
        setAttribute("data-native", "")
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _,old,new ->
        if (new.size != old.size) {
            updateScroll()
        }
    }

    private val contentBoundsChanged: (View, Rectangle, Rectangle) -> Unit = { view, old, new ->
        if (new.size != old.size) {
            spacerDiv.style.setSize(view.size)
            updateScroll()
        }
    }

    private val contentChanged: (ScrollPanel, View?, View?) -> Unit = { _,old,new ->
        old?.let { view ->
            graphicsDevice[view].let {
                it.rootElement.style.willChange = ""

                it.removedFromNativeScroll()
                rootElement.remove(spacerDiv)
                rootElement.style.setOverflow()

                view.boundsChanged  -= contentBoundsChanged
                panel.boundsChanged -= boundsChanged
            }
        }

        new?.let { view ->
            graphicsDevice[view].let {
                it.addedToNativeScroll()
                it.rootElement.style.willChange = "transform"

                rootElement.style.setOverflow(Auto())
                rootElement.add(spacerDiv)

                spacerDiv.add(it.rootElement.parent!!)
                updateScroll()

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

    private var barWidth  = scrollBarSize
    private var barHeight = barWidth

    private val observer = ResizeObserver { updates,_ ->
        try {
            val entry = updates.first()

            if (entry.contentRect.width > 0 && rootElement.width > 0) {
                barChanged(Vertical, entry.run { panel.width - contentRect.width }.also { barWidth = it })
            }
            if (entry.contentRect.height > 0 && rootElement.height > 0) {
                barChanged(Horizontal, entry.run { panel.height - contentRect.height }.also { barHeight = it })
            }
        } catch (ignored: Throwable) {
            barWidth  = scrollBarSize
            barHeight = barWidth
        }

        updateScroll()
    }

    init {
        observer.observe(rootElement, object: ResizeObserverInit {
            override var box: String? = "content-box"
        })

        rootElement.apply {
            updateScroll()

            if (smoothScroll) {
                style.scrollBehavior = "smooth"
            }

            scrollTo(panel.scroll)
        }

        eventHandler = handlerFactory(rootElement, this).apply {
            registerScrollListener()
        }

        panel.contentChanged += contentChanged

        barChanged(Vertical,   barWidth )
        barChanged(Horizontal, barHeight)

        contentChanged(panel, null, panel.content)
    }

    fun discard() {
        panel.boundsChanged  -= boundsChanged
        panel.contentChanged -= contentChanged

        rootElement.apply {
            style.setOverflow(Hidden())

            removeAttribute("data-native")
            eventHandler.unregisterScrollListener()
        }

        observer.unobserve(rootElement)
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

    private fun updateScroll() {
        val size = when (val content = panel.content) {
            null -> panel.size
            else -> Size(min(content.width, max(0.0, panel.width - barWidth)), min(content.height, max(0.0, panel.height - barHeight)))
        }

        (spacerDiv.firstChild as? HTMLElement)?.style?.setSize(size)
    }
}