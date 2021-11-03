package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Size
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skiko.SkiaWindow
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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
    private val button: HyperLink? = button

    override var selected_             = false
    override var ignoreSelectionChange = false

    init {
        text                      = "<html><a href='${button.url}'>${button.text}</a></html>"
        focusTraversalKeysEnabled = false

        button.size = preferredSize.run { Size(width, height) }

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

        addMouseListener(object : MouseAdapter() {
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

    override fun handleMouseEvent(e: MouseEvent?) {
        processMouseEvent(e)
    }
}


internal class NativeHyperLinkBehavior(
        window                   : SkiaWindow,
        appScope                 : CoroutineScope,
        uiDispatcher             : CoroutineContext,
        swingGraphicsFactory     : SwingGraphicsFactory,
        textMetrics              : TextMetrics,
        swingFocusManager        : javax.swing.FocusManager,
        focusManager             : FocusManager?,
        nativePointerPreprocessor: NativePointerPreprocessor?
): AbstractNativeButtonBehavior<HyperLink, HyperLinkPeer>(window, appScope, uiDispatcher, textMetrics, swingGraphicsFactory, swingFocusManager, focusManager, nativePointerPreprocessor) {
    override fun createPeer(button: HyperLink) = HyperLinkPeer(focusManager, button)
}