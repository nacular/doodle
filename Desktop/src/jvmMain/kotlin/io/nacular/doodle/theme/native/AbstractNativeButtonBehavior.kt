package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.SystemPointerEvent.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.coroutines.CoroutineContext


internal abstract class AbstractNativeButtonBehavior<in T: Button, P>(
        private   val window                   : JPanel,
        private   val appScope                 : CoroutineScope,
        private   val uiDispatcher             : CoroutineContext,
                      textMetrics              : TextMetrics,
        private   val swingGraphicsFactory     : SwingGraphicsFactory,
        private   val swingFocusManager        : javax.swing.FocusManager,
        protected val focusManager             : FocusManager?,
        private   val nativePointerPreprocessor: NativePointerPreprocessor?
): CommonTextButtonBehavior<T>(textMetrics, focusManager = focusManager) where P: JComponent, P: AbstractNativeButtonBehavior.Peer {

    internal interface Peer {
        var selected_            : Boolean
        var ignoreSelectionChange: Boolean

        fun handleMouseEvent(e: MouseEvent?)
    }

    protected abstract fun createPeer (button: T         ): P
    protected open     fun destroyPeer(button: T, peer: P) {}

    private lateinit var nativePeer   : P
    private          var oldCursor    : Cursor? = null
    private          var oldIdealSize : Size?   = null

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        when (new) {
            true -> if (!nativePeer.hasFocus()) nativePeer.requestFocusInWindow()
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
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
    }

    override fun render(view: T, canvas: Canvas) {
        nativePeer.paint(swingGraphicsFactory((canvas as CanvasImpl).skiaCanvas))
    }

    override fun mirrorWhenRightToLeft(view: T) = false

    override fun install(view: T) {
        super.install(view)

        if (this::nativePeer.isInitialized) {
            destroyPeer(view, nativePeer)
        }

        nativePeer = createPeer(view)

        nativePointerPreprocessor?.set(view, object: NativePointerHandler {
            override fun invoke(event: PointerEvent) {
                if (event.source == view) {
                    nativePeer.ignoreSelectionChange = event.type == Type.Up
                    nativePeer.handleMouseEvent(event.toAwt(nativePeer))
                    nativePeer.ignoreSelectionChange = false
                }
            }
        })

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

            window.add(nativePeer)

            if (view.hasFocus) {
                nativePeer.requestFocusInWindow()
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

        nativePointerPreprocessor?.remove(view)

        appScope.launch(uiDispatcher) {
            window.remove(nativePeer)

            destroyPeer(view, nativePeer)
        }
    }

    override val selectionChanged: (Button, Boolean, Boolean) -> Unit = { button, old, new ->
        super.selectionChanged(button, old, new)

        if (!nativePeer.ignoreSelectionChange) {
            nativePeer.selected_ = new
        }
    }

//    override fun entered(event: PointerEvent) {
//        super.entered(event)
//        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
//    }
//
//    override fun exited(event: PointerEvent) {
//        super.exited(event)
//        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
//    }
//
//    override fun pressed(event: PointerEvent) {
//        super.pressed(event)
//        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
//    }
//
//    override fun clicked(event: PointerEvent) {
//        super.clicked(event)
//        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
//    }
//
//    override fun released(event: PointerEvent) {
//        nativePeer.ignoreSelectionChange = true
//        super.released(event)
//        nativePeer.handleMouseEvent(event.toAwt(nativePeer))
//        nativePeer.ignoreSelectionChange = false
//    }
}