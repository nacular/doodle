package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.native.NativeTheme.WindowDiscovery
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skia.FontMgr
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.MOUSE_DRAGGED
import java.awt.event.MouseEvent.MOUSE_MOVED
import java.net.URI
import javax.swing.JLabel
import kotlin.coroutines.CoroutineContext

internal class NativeHyperLinkStylerImpl: NativeHyperLinkStyler {
    override fun invoke(hyperLink: HyperLink, behavior: Behavior<HyperLink>): Behavior<HyperLink> = NativeHyperLinkBehaviorWrapper(
            hyperLink,
            behavior
    )
}

private class NativeHyperLinkBehaviorWrapper(
        hyperLink             : HyperLink,
        private val delegate  : Behavior<HyperLink>): Behavior<HyperLink> by delegate {

    init {
        hyperLink.fired += {
            try {
                java.awt.Desktop.getDesktop().browse(URI(hyperLink.url))
            } catch (ignored: Exception) {
            }
        }
    }
}

internal class HyperLinkPeer(focusManager: FocusManager?, button: HyperLink): JLabel(), AbstractNativeButtonBehavior.Peer, PointerListener {
    // This needs to remain since JLabel will render on construct before the local value is initialized
    @Suppress("RedundantNullableReturnType")
    private val button: HyperLink? = button

    override var selected_             = false
    override var ignoreSelectionChange = false

    init {
        text                      = "<html><a href='${button.url}'>${button.text}</a></html>"
        focusTraversalKeysEnabled = false

        button.suggestSize(preferredSize.run { Size(width, height) })

        addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent?) {
                focusManager?.requestFocus(button)
            }

            override fun focusLost(e: FocusEvent?) {
                if (button == focusManager?.focusOwner) {
                    focusManager.clearFocus()
                }
            }
        })

        button.pointerChanged += this

        addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                try {
                    java.awt.Desktop.getDesktop().browse(URI(button.url))
                } catch (ignored: Exception) {
                }
            }
        })
    }

//    override fun clicked(event: PointerEvent) {
//        try {
//            java.awt.Desktop.getDesktop().browse(URI(button!!.url))
//        } catch (ignored: Exception) {
//        }
//    }

    override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
        button?.rerender()
    }

    override fun handleMouseEvent(e: MouseEvent?) = when (e?.id) {
        MOUSE_MOVED, MOUSE_DRAGGED -> processMouseMotionEvent(e)
        else                       -> processMouseEvent      (e)
    }
}


internal class NativeHyperLinkBehavior(
    window                   : WindowDiscovery,
    appScope                 : CoroutineScope,
    uiDispatcher             : CoroutineContext,
    fontManager              : FontMgr,
    swingGraphicsFactory     : SwingGraphicsFactory,
    textMetrics              : TextMetrics,
    swingFocusManager        : javax.swing.FocusManager,
    focusManager             : FocusManager?,
    nativePointerPreprocessor: NativePointerPreprocessor?
): AbstractNativeButtonBehavior<HyperLink, HyperLinkPeer>(
    window,
    appScope,
    uiDispatcher,
    textMetrics,
    fontManager,
    swingGraphicsFactory,
    swingFocusManager,
    focusManager,
    nativePointerPreprocessor
) {
    override fun createPeer(button: HyperLink) = HyperLinkPeer(focusManager, button)
}