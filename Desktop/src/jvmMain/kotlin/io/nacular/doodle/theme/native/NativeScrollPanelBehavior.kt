package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.panels.ScrollPanelBehavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.LookupResult
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.drawing.impl.RealGraphicsSurface
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.SystemPointerEvent.Type.Click
import io.nacular.doodle.system.SystemPointerEvent.Type.Down
import io.nacular.doodle.system.SystemPointerEvent.Type.Drag
import io.nacular.doodle.system.SystemPointerEvent.Type.Up
import io.nacular.doodle.system.impl.NativeScrollHandler
import io.nacular.doodle.system.impl.NativeScrollHandlerFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseWheelEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JViewport
import javax.swing.Scrollable
import kotlin.coroutines.CoroutineContext


/**
 * Created by Nicholas Eddy on 6/29/21.
 */
internal class NativeScrollPanelBehavior(
        private val window                   : JPanel,
        private val appScope                 : CoroutineScope,
        private val uiDispatcher             : CoroutineContext,
        private val graphicsDevice           : GraphicsDevice<RealGraphicsSurface>,
        private val swingGraphicsFactory     : SwingGraphicsFactory,
        private val nativeScrollHandlerFinder: NativeScrollHandlerFinder?,
        private val nativePointerPreprocessor: NativePointerPreprocessor?): ScrollPanelBehavior {

    private inner class JScrollPanePeer(scrollPanel: ScrollPanel): JScrollPane() {
        private val scrollPanel: ScrollPanel? = scrollPanel

        inner class ViewPortComponent: JComponent(), Scrollable {
            init {
                scrollPanel?.content?.let {
                    bounds        = java.awt.Rectangle(it.x.toInt(), it.y.toInt(), it.width.toInt(), it.height.toInt())
                    preferredSize = size
                }
            }

            override fun getPreferredScrollableViewportSize(): Dimension = preferredSize
            override fun getScrollableUnitIncrement        (visibleRect: java.awt.Rectangle?, orientation: Int, direction: Int) =  1
            override fun getScrollableBlockIncrement       (visibleRect: java.awt.Rectangle?, orientation: Int, direction: Int) = 10
            override fun getScrollableTracksViewportWidth  () = false
            override fun getScrollableTracksViewportHeight () = false
        }

        inner class TransparentViewport: JViewport() {
            init {
                this.isOpaque = false
            }

            public override fun paintComponent(g: Graphics) {}
        }

        var corner = JPanel().apply {
            background = horizontalScrollBar.background ?: this@JScrollPanePeer.background
        }
        var viewPortView = ViewPortComponent()

        init {
            border = null
            setViewport(TransparentViewport())
            setViewportView(viewPortView)

            setCorner(LOWER_RIGHT_CORNER, corner)

            viewport.addChangeListener {
                onScroll?.invoke(Point(horizontalScrollBar.value, verticalScrollBar.value))
            }
        }

        override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
            scrollPanel?.rerender()
        }

        override fun paintChildren(g: Graphics?) {
            // no-op
        }

        fun actuallyPaintChildren(g: Graphics) {
            super.paintChildren(g)
        }
    }

    private var oldCursor    : Cursor?    = null
    private var oldIdealSize : Size?      = null
    private var clickedTarget: Component? = null

    private lateinit var nativePeer     : JScrollPanePeer
    private lateinit var graphics2D     : SkiaGraphics2D
    private lateinit var graphicsSurface: RealGraphicsSurface

    private val postRender: ((Canvas) -> Unit) = {
        nativePeer.actuallyPaintChildren(graphics2D)
    }

    override var onScroll: ((Point) -> Unit)? = null

    override fun scrollTo(panel: ScrollPanel, point: Point) {
        // no-op
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _, _, new ->
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
        nativePeer.revalidate()
    }

    private val contentBoundsChanged: (source: View, old: Rectangle, new: Rectangle) -> Unit = { _, _, new ->
        updateNativePeerScroll(new)
    }

    private val contentChanged: (source: ScrollPanel, old: View?, new: View?) -> Unit = { _, old, new ->
        old?.boundsChanged?.minusAssign(contentBoundsChanged)
        new?.boundsChanged?.plusAssign (contentBoundsChanged)

        updateNativePeerScroll(new?.bounds ?: Rectangle.Empty)
    }

    private fun updateNativePeerScroll(bounds: Rectangle) {
        nativePeer.viewPortView.location      = bounds.position.run { java.awt.Point(x.toInt(), y.toInt()) }
        nativePeer.viewPortView.preferredSize = bounds.size.run     { Dimension(width.toInt(), height.toInt()) }
        nativePeer.viewPortView.revalidate()
        window.revalidate()
    }

    override fun render(view: ScrollPanel, canvas: Canvas) {
        graphics2D = swingGraphicsFactory((canvas as CanvasImpl).skiaCanvas)

        nativePeer.paint(graphics2D)
    }

    override fun mirrorWhenRightToLeft(view: ScrollPanel) = false

    private inner class LayoutWrapper(delegate: Layout): Layout by delegate {
        override fun child(of: PositionableContainer, at: Point): LookupResult {
            val target = when {
                this@NativeScrollPanelBehavior::nativePeer.isInitialized -> nativePeer.getComponentAt(at.x.toInt(), at.y.toInt())
                else                                                     -> null
            }

            return when (target) {
                null, nativePeer.viewport, nativePeer.viewPortView, nativePeer -> super.child(of, at)
                else                                                           -> LookupResult.Empty
            }
        }
    }

    override fun install(view: ScrollPanel) {
        super.install(view)

        view.layout?.let {
            view.layout = LayoutWrapper(it)
        }

        if (this::graphicsSurface.isInitialized) {
            graphicsSurface.postRender = null
        }

        graphicsSurface = graphicsDevice[view].also { it.postRender = postRender }

        nativePeer = JScrollPanePeer(view)

        nativeScrollHandlerFinder?.set(view, object: NativeScrollHandler {
            override fun invoke(event: MouseWheelEvent, pointInTarget: Point) {
                nativePeer.dispatchEvent(MouseWheelEvent(
                        nativePeer,
                        event.id,
                        event.`when`,
                        event.modifiersEx,
                        event.x,
                        event.y,
                        event.clickCount,
                        event.isPopupTrigger,
                        event.scrollType,
                        event.scrollAmount,
                        event.wheelRotation
                ))
            }
        })

        nativePointerPreprocessor?.set(view, object: NativeMouseHandler {
            override fun invoke(event: PointerEvent) {
                processPointerEvent(event)
            }
        })

        view.apply {
            oldIdealSize    = idealSize
            boundsChanged  += this@NativeScrollPanelBehavior.boundsChanged
            contentChanged += this@NativeScrollPanelBehavior.contentChanged

            content?.boundsChanged?.plusAssign(this@NativeScrollPanelBehavior.contentBoundsChanged)
        }

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            view.apply {
                cursor    = Cursor.Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }

            window.add(nativePeer)
            window.revalidate()

            if (view.hasFocus) {
                nativePeer.requestFocusInWindow()
            }
        }
    }

    override fun uninstall(view: ScrollPanel) {
        super.uninstall(view)

        if (this::graphicsSurface.isInitialized) {
            graphicsSurface.postRender = null
        }

        view.apply {
            cursor          = oldCursor
            idealSize       = oldIdealSize
            boundsChanged  -= this@NativeScrollPanelBehavior.boundsChanged
            contentChanged -= this@NativeScrollPanelBehavior.contentChanged

            content?.boundsChanged?.minusAssign(this@NativeScrollPanelBehavior.contentBoundsChanged)
        }

        nativeScrollHandlerFinder?.remove(view)
        nativePointerPreprocessor?.remove(view)

        appScope.launch(uiDispatcher) {
            window.remove(nativePeer)
        }
    }

    private fun processPointerEvent(event: PointerEvent) {
        when (event.type) {
            Drag -> clickedTarget
            else -> nativePeer.getComponentAt(event.location.x.toInt(), event.location.y.toInt())
        }?.let { target ->
            clickedTarget = when (event.type) {
                Click -> target
                Down  -> target
                Up    -> null
                else       -> clickedTarget
            }

            val at = when (target) {
                nativePeer -> event.location
                else       -> Point(event.location.x - target.x, event.location.y - target.y)
            }

            target.dispatchEvent(event.toAwt(target, at))

            if (target != nativePeer && target != nativePeer.viewport && target != nativePeer.corner) {
                event.consume()
            }
        }
    }
}