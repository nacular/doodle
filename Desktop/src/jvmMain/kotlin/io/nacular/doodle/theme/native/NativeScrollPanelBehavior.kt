package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.panels.ScrollPanelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.skia.toImage
import io.nacular.doodle.system.Cursor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.SkiaWindow
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.Scrollable
import kotlin.coroutines.CoroutineContext

/**
 * Created by Nicholas Eddy on 6/29/21.
 */
internal class NativeScrollPanelBehavior(
        private val window      : SkiaWindow,
        private val appScope    : CoroutineScope,
        private val uiDispatcher: CoroutineContext,
        private val contentScale: Double
): ScrollPanelBehavior, PointerListener, PointerMotionListener {

    private inner class JScrollPanePeer(scrollPanel: ScrollPanel): JScrollPane() {
        private val scrollPanel: ScrollPanel? = scrollPanel

        inner class ViewPortComponent: JComponent(), Scrollable {
            init {
                scrollPanel?.content?.let {
                    bounds        = java.awt.Rectangle(it.x.toInt(), it.y.toInt(), it.width.toInt(), it.height.toInt())
                    preferredSize = size
                }
            }

            override fun getPreferredScrollableViewportSize(): Dimension = size
            override fun getScrollableUnitIncrement        (visibleRect: java.awt.Rectangle?, orientation: Int, direction: Int) = 100
            override fun getScrollableBlockIncrement       (visibleRect: java.awt.Rectangle?, orientation: Int, direction: Int) = 100
            override fun getScrollableTracksViewportWidth  () = false
            override fun getScrollableTracksViewportHeight () = false
        }

        var viewPortView = ViewPortComponent()

        init {
            setViewportView(viewPortView)

            viewport.addChangeListener {
                onScroll?.invoke(Point(horizontalScrollBar.value, verticalScrollBar.value))
            }
        }

        override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
            super.repaint(tm, x, y, width, height)
            scrollPanel?.rerender()
        }
    }

    private lateinit var nativePeer: JScrollPanePeer
    private var oldCursor   : Cursor? = null
    private var oldIdealSize: Size?   = null
    private lateinit var bufferedImage: BufferedImage
    private lateinit var graphics     : Graphics2D

    override var onScroll: ((Point) -> Unit)? = null

    override fun scrollTo(panel: ScrollPanel, point: Point) {
        nativePeer.scrollRectToVisible(java.awt.Rectangle(point.x.toInt(), point.y.toInt(), 1, 1))
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _,_,new ->
        createNewBufferedImage(new.size)
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
        window.revalidate()
    }

    private val contentBoundsChanged: (source: View, old: Rectangle, new: Rectangle) -> Unit = { _,_,new ->
        updateNativePeerScroll(new)
    }

    private val contentChanged: (source: ScrollPanel, old: View?, new: View?) -> Unit = { _,old,new ->
        old?.boundsChanged?.minusAssign(contentBoundsChanged)
        new?.boundsChanged?.plusAssign (contentBoundsChanged)

        updateNativePeerScroll(new?.bounds ?: Rectangle.Empty)
    }

    private fun updateNativePeerScroll(bounds: Rectangle) {
        nativePeer.viewPortView.bounds = bounds.run { java.awt.Rectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt()) }
    }

    private fun createNewBufferedImage(size: Size) {
        if (!size.empty && contentScale > 0.0) {
            bufferedImage = BufferedImage((size.width * contentScale).toInt(), (size.height * contentScale).toInt(), BufferedImage.TYPE_INT_ARGB)
            this@NativeScrollPanelBehavior.graphics = bufferedImage.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                scale(contentScale, contentScale)
            }
        }
    }

    override fun render(view: ScrollPanel, canvas: Canvas) {
        if (this::graphics.isInitialized) {
            graphics.background = Color(255, 255, 255, 0)
            graphics.clearRect(0, 0, bufferedImage.width, bufferedImage.height)

            nativePeer.paint(graphics)

            canvas.scale(1 / contentScale, 1 / contentScale) {
                canvas.image(ImageImpl(bufferedImage.toImage(), ""))
            }
        }
    }

    override fun mirrorWhenRightToLeft(view: ScrollPanel) = false

    override fun install(view: ScrollPanel) {
        super.install(view)

        nativePeer = JScrollPanePeer(view)

        view.apply {
            oldIdealSize          = idealSize
            boundsChanged        += this@NativeScrollPanelBehavior.boundsChanged
            pointerChanged       += this@NativeScrollPanelBehavior
            contentChanged       += this@NativeScrollPanelBehavior.contentChanged
            pointerMotionChanged += this@NativeScrollPanelBehavior

            content?.boundsChanged?.plusAssign(this@NativeScrollPanelBehavior.contentBoundsChanged)
        }

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            view.apply {
                cursor    = Cursor.Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }

            createNewBufferedImage(view.size)
            window.add(nativePeer)
            window.revalidate()
        }
    }

    override fun uninstall(view: ScrollPanel) {
        super.uninstall(view)

        view.apply {
            cursor                = oldCursor
            idealSize             = oldIdealSize
            boundsChanged        -= this@NativeScrollPanelBehavior.boundsChanged
            pointerChanged       -= this@NativeScrollPanelBehavior
            contentChanged       -= this@NativeScrollPanelBehavior.contentChanged
            pointerMotionChanged -= this@NativeScrollPanelBehavior

            content?.boundsChanged?.minusAssign(this@NativeScrollPanelBehavior.contentBoundsChanged)
        }

        appScope.launch(uiDispatcher) {
            window.remove(nativePeer)
        }
    }


    private fun processPointerEvent(event: PointerEvent) {
        nativePeer.getComponentAt(event.location.x.toInt(), event.location.y.toInt())?.let { target ->
            val at = when (target) {
                nativePeer -> event.location
                else       -> Point(event.location.x - target.x, event.location.y - target.y)
            }

            target.dispatchEvent(event.toAwt(target, at))
            println("event [${event.type}] -> ${target::class.simpleName} @$at")
        }
    }

    override fun entered(event: PointerEvent) {
        processPointerEvent(event)
    }

    override fun exited(event: PointerEvent) {
        processPointerEvent(event)
    }

    override fun pressed(event: PointerEvent) {
        processPointerEvent(event)
    }

    override fun released(event: PointerEvent) {
        processPointerEvent(event)
    }

    override fun moved(event: PointerEvent) {
        processPointerEvent(event)
    }

    override fun dragged(event: PointerEvent) {
        processPointerEvent(event)
    }
}