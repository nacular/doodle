package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.skia.toImage
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.system.SystemPointerEvent.Type.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skiko.SkiaWindow
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import javax.swing.JButton
import kotlin.coroutines.CoroutineContext


/**
 * Created by Nicholas Eddy on 6/14/21.
 */
internal class NativeButtonBehavior(
        private val appScope: CoroutineScope,
        private val uiDispatcher: CoroutineContext,
        private val window: SkiaWindow,
        textMetrics: TextMetrics,
        private val focusManager: FocusManager?,
        private val button: Button
): CommonTextButtonBehavior<Button>(textMetrics) {

    private inner class JButtonPeer(button: Button): JButton(), PointerListener {
        private val textChanged: (View, String, String) -> Unit = { _, _, _ ->
            button.rerender()
        }

        private val styleChanged: (View) -> Unit = {
//            font = it.font
            button.rerender()
        }

        private val focusChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
            when (new) {
                true -> requestFocus()
                else -> transferFocus()
            }
        }

        private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
            isEnabled = new
        }

        private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
            isFocusable = new
        }

        private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _, _, new ->
            size = new.size.run { Dimension(width.toInt(), height.toInt()) }
        }

        init {
            text = button.text

            button.apply {
                textChanged         += this@JButtonPeer.textChanged
                focusChanged        += this@JButtonPeer.focusChanged
                styleChanged        += this@JButtonPeer.styleChanged
                boundsChanged       += this@JButtonPeer.boundsChanged
                enabledChanged      += this@JButtonPeer.enabledChanged
                pointerChanged      += this@JButtonPeer
                focusabilityChanged += this@JButtonPeer.focusableChanged
            }

            addFocusListener(object : FocusListener {
                override fun focusGained(e: FocusEvent?) {
                    focusManager?.requestFocus(button)
                }

                override fun focusLost(e: FocusEvent?) {
                    focusManager?.clearFocus()
                }
            })
        }

        override fun repaint() {
            super.repaint()
            button.rerender()
        }

        override fun entered(event: PointerEvent) {
            processMouseEvent(event.toAwt(this))
        }

        override fun exited(event: PointerEvent) {
            processMouseEvent(event.toAwt(this))
        }

        override fun pressed(event: PointerEvent) {
            processMouseEvent(event.toAwt(this))
        }

        override fun released(event: PointerEvent) {
            processMouseEvent(event.toAwt(this))
        }
    }

    private val nativePeer by lazy { JButtonPeer(button) }
    private var oldCursor   : Cursor? = null
    private var oldIdealSize: Size?   = null
    private var bufferedImage = BufferedImage(button.width.toInt(), button.height.toInt(), TYPE_INT_ARGB)
    private val graphics      = bufferedImage.createGraphics()

    override fun render(view: Button, canvas: Canvas) {
        graphics.background = Color(255, 255, 255, 0)
        graphics.clearRect(0, 0, nativePeer.width, nativePeer.height)

        nativePeer.paint(graphics)
        canvas.image(ImageImpl(bufferedImage.toImage(), ""))
    }

    override fun mirrorWhenRightToLeft(view: Button) = false

    override fun install(view: Button) {
        super.install(view)

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            view.apply {
                cursor    = Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }
        }
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

//        appScope.launch(uiDispatcher) {
//            graphicsDevice[view].skiaPanel.remove(nativePeer)
//        }

        view.apply {
            cursor    = oldCursor
            idealSize = oldIdealSize
        }
    }

    override fun released(event: PointerEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons.isEmpty()) {
            model.pressed = false
            model.armed   = false
        }
    }

    override fun pointerChanged(button: Button) {}

    override fun released(event: KeyEvent) {}

    override fun pressed(event: KeyEvent) {}
}