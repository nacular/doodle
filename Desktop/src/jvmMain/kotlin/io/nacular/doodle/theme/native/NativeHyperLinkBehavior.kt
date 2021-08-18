package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skiko.SkiaWindow
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JLabel
import kotlin.coroutines.CoroutineContext

/**
 * Allows full control over how native [HyperLink]s are styled. The given behavior is delegated
 * to for all visual styling, but the app will also treat the view as it does un-styled links.
 */
public interface NativeHyperLinkBehaviorBuilder {
    /**
     * Wraps [behavior] with other native styling for hyper links.
     *
     * @param behavior to be "wrapped"
     * @return a new Behavior for the link
     */
    public operator fun invoke(hyperLink: HyperLink, behavior: Behavior<HyperLink>): Behavior<HyperLink>
}

internal class NativeHyperLinkBehaviorBuilderImpl: NativeHyperLinkBehaviorBuilder {
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


internal class HyperLinkPeer(focusManager: FocusManager?, button: HyperLink): JLabel(), AbstractNativeButtonBehavior.Peer {
    private val button: Button? = button

    override var selected_             = false
    override var ignoreSelectionChange = false
    override var clip: Rectangle?      = null

    init {
        text = "<html><a href='${button.url}'>${button.text}</a></html>"

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

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                try {
                    java.awt.Desktop.getDesktop().browse(URI(button.url))
                } catch (ignored: Exception) {
                }
            }

            override fun mouseEntered(e: MouseEvent?) {
                text = "<html><a href='${button.url}'>${button.text}</a></html>"
            }

            override fun mouseExited(e: MouseEvent?) {
                text = button.text
            }
        })
    }

    override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
        clip = clip?.union(Rectangle(x, y, width, height)) ?: Rectangle(x, y, width, height)
        button?.rerender()
    }

    override fun handleMouseEvent(e: MouseEvent?) {
        processMouseEvent(e)
    }
}


internal class NativeHyperLinkBehavior(
        window              : SkiaWindow,
        appScope            : CoroutineScope,
        uiDispatcher        : CoroutineContext,
        swingGraphicsFactory: SwingGraphicsFactory,
        textMetrics         : TextMetrics,
        swingFocusManager   : javax.swing.FocusManager,
        focusManager        : FocusManager?
): AbstractNativeButtonBehavior<Button, JButtonPeer>(window, appScope, uiDispatcher, textMetrics, swingGraphicsFactory, swingFocusManager, focusManager) {
    override fun createPeer(button: Button) = JButtonPeer(focusManager, button)
}