package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.system.Cursor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.SkiaWindow
import java.awt.Color
import java.awt.Dimension
import java.awt.GraphicsConfiguration
import java.awt.event.MouseEvent
import javax.swing.JComponent
import kotlin.coroutines.CoroutineContext


internal abstract class AbstractNativeButtonBehavior<T : Button, P>(
                      graphicsConfiguration: GraphicsConfiguration,
        private   val window               : SkiaWindow,
        private   val appScope             : CoroutineScope,
        private   val uiDispatcher         : CoroutineContext,
        private   val contentScale         : Double,
                      textMetrics          : TextMetrics,
        private   val swingFocusManager    : javax.swing.FocusManager,
        protected val focusManager         : FocusManager?
): CommonTextButtonBehavior<T>(textMetrics) where P: JComponent, P: AbstractNativeButtonBehavior.Peer {

    internal interface Peer {
        var selected_            : Boolean
        var ignoreSelectionChange: Boolean
        var clip                 : Rectangle?

        fun handleMouseEvent(e: MouseEvent?)
    }

    protected abstract fun createPeer(button: T): P

    private lateinit var nativePeer: P
    private          var oldCursor    : Cursor? = null
    private          var oldIdealSize : Size?   = null
    private          val offscreenGraphics = OffscreenGraphics(graphicsConfiguration, contentScale)

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        when (new) {
            true -> if (!nativePeer.hasFocus()) nativePeer.requestFocus()
            else -> swingFocusManager.clearFocusOwner()
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        nativePeer.isEnabled = new
    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        nativePeer.isFocusable = new
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _, _, new ->
        offscreenGraphics.size = new.size
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
    }

    override fun render(view: T, canvas: Canvas) {
        offscreenGraphics.render { graphics ->
            val clip = when (nativePeer.clip?.empty) {
                false -> nativePeer.clip!!.run { java.awt.Rectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt()) }
                else  -> java.awt.Rectangle(0, 0, view.width.toInt(), view.height.toInt())
            }

            graphics.clip = clip.also {
                graphics.background = Color(255, 255, 255, 0)
                graphics.clearRect(it.x, it.y, it.width, it.height)
            }

            nativePeer.paint(graphics)
        }.let {
            canvas.scale(1 / contentScale, 1 / contentScale) {
                canvas.image(ImageImpl(it, ""))
            }
        }
    }

    override fun mirrorWhenRightToLeft(view: T) = false

    override fun install(view: T) {
        super.install(view)

        nativePeer = createPeer(view)

        view.apply {
            oldIdealSize         = idealSize
            focusChanged        += this@AbstractNativeButtonBehavior.focusChanged
            boundsChanged       += this@AbstractNativeButtonBehavior.boundsChanged
            enabledChanged      += this@AbstractNativeButtonBehavior.enabledChanged
            focusabilityChanged += this@AbstractNativeButtonBehavior.focusableChanged
        }

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            view.apply {
                cursor    = Cursor.Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }

            offscreenGraphics.size = view.size
//            createNewBufferedImage(view.size)

            window.add(nativePeer)

            if (view.hasFocus) {
                nativePeer.requestFocus()
            }
        }
    }

    override fun uninstall(view: T) {
        super.uninstall(view)

        view.apply {
            cursor    = oldCursor
            idealSize = oldIdealSize

            focusChanged        -= this@AbstractNativeButtonBehavior.focusChanged
            boundsChanged       -= this@AbstractNativeButtonBehavior.boundsChanged
            enabledChanged      -= this@AbstractNativeButtonBehavior.enabledChanged
            focusabilityChanged -= this@AbstractNativeButtonBehavior.focusableChanged
        }

        appScope.launch(uiDispatcher) {
            window.remove(nativePeer)
        }
    }

    override val selectionChanged: (Button, Boolean, Boolean) -> Unit = { button, old, new ->
        super.selectionChanged(button, old, new)

        if (!nativePeer.ignoreSelectionChange) {
            nativePeer.selected_ = new
        }
    }

    override fun entered(event: PointerEvent) {
        super.entered(event)
        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
    }

    override fun exited(event: PointerEvent) {
        super.exited(event)
        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
    }

    override fun pressed(event: PointerEvent) {
        super.pressed(event)
        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
    }

    override fun released(event: PointerEvent) {
        nativePeer.ignoreSelectionChange = true
        super.released(event)
        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
        nativePeer.ignoreSelectionChange = false
    }
}