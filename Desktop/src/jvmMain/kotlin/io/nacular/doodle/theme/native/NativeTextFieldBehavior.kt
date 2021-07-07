package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.text.Selection
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFieldBehavior
import io.nacular.doodle.controls.text.TextInput
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
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
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import javax.swing.JLabel
import javax.swing.JPasswordField
import javax.swing.border.Border
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent
import kotlin.coroutines.CoroutineContext

private class PlaceHolderLabel(text: String, private val component: JTextComponent): JLabel(), DocumentListener {
    private fun updateVisibility() {
        isVisible = component.document.length == 0
    }

    override fun insertUpdate (e: DocumentEvent) { updateVisibility() }
    override fun removeUpdate (e: DocumentEvent) { updateVisibility() }
    override fun changedUpdate(e: DocumentEvent) {                    }

    init {
        addComponentListener(object: ComponentAdapter() {
            override fun componentShown(e: ComponentEvent) {
                component.revalidate()
            }
        })

        font                = component.font
        border              = null
        this.text           = text
        foreground          = component.foreground
        horizontalAlignment = LEADING

        component.layout = BorderLayout()
        component.document.addDocumentListener(this)
        component.add(this)

        updateVisibility()
    }
}
/**
 * Created by Nicholas Eddy on 6/14/21.
 */
internal class NativeTextFieldBehavior(
        private val window: SkiaWindow,
        private val appScope: CoroutineScope,
        private val uiDispatcher: CoroutineContext,
        private val contentScale: Double,
        private val swingFocusManager: javax.swing.FocusManager,
        private val focusManager: FocusManager?
): TextFieldBehavior, PointerListener, PointerMotionListener {

    private inner class JTextFieldPeer(textField: TextField): JPasswordField() {
        private var textField: TextField? = textField

        var placeHolder: String
            get()    = placeHolderLabel?.text ?: ""
            set(new) {
                placeHolderLabel = when (new) {
                    ""   -> null
                    else -> PlaceHolderLabel(new, this)
                }
            }

        private var placeHolderLabel: PlaceHolderLabel? = null

        init {
            text          = textField.text
            echoChar      = textField.mask ?: 0.toChar()
            placeHolder   = textField.placeHolder
            defaultBorder = border

            if (!textField.borderVisible) { border = null }

            addFocusListener(object : FocusListener {
                override fun focusGained(e: FocusEvent?) {
                    if (textField != focusManager?.focusOwner) {
                        println("[Swing -> Doodle] requesting focus"); focusManager?.requestFocus(textField)
                    }
                }

                override fun focusLost(e: FocusEvent?) {
                    if (textField == focusManager?.focusOwner) {
                        println("[Swing -> Doodle] clearing focus"); focusManager.clearFocus()
                    }
                }
            })
        }

        override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
            super.repaint(tm, x, y, width, height)
            textField?.rerender()
        }

        public override fun processMouseEvent(e: MouseEvent?) {
            super.processMouseEvent(e)
        }

        public override fun processMouseMotionEvent(e: MouseEvent?) {
            super.processMouseMotionEvent(e)
        }
    }

    private lateinit var defaultBorder: Border
    private lateinit var graphics     : Graphics2D
    private          var oldCursor    : Cursor? = null
    private lateinit var nativePeer   : JTextFieldPeer
    private          var oldIdealSize : Size?   = null
    private lateinit var bufferedImage: BufferedImage

    private val maskChanged = { _: TextField, _: Char?, new: Char? ->
        nativePeer.echoChar = new ?: 0.toChar()
    }

    private val textChanged: (TextInput, String, String) -> Unit = { _, _, new ->
        nativePeer.text = new
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        when (new) {
            true -> if (!nativePeer.hasFocus()) {
                println("[Doodle -> Swing] requesting focus"); nativePeer.requestFocus()
            }
            else -> if ( nativePeer.hasFocus()) { println("[Doodle -> Swing] clearing focus"); swingFocusManager.clearFocusOwner() }
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        nativePeer.isEnabled = new
    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        nativePeer.isFocusable = new
    }

    private val styleChanged: (source: View) -> Unit = {
        (it as? TextField)?.let { textField ->
            nativePeer.border = when {
                textField.borderVisible -> defaultBorder
                else                    -> null
            }

            nativePeer.placeHolder = textField.placeHolder

//        nativePeer.
        }
    }

    private val selectionChanged: (source: TextInput, old: Selection, new: Selection) -> Unit = { _, _, new ->
        nativePeer.select(new.start, new.end)
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _, _, new ->
        createNewBufferedImage(new.size)
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
        nativePeer.revalidate()
    }

    private fun createNewBufferedImage(size: Size) {
        if (!size.empty && contentScale > 0f) {
            bufferedImage = BufferedImage((size.width * contentScale).toInt(), (size.height * contentScale).toInt(), TYPE_INT_ARGB)
            this@NativeTextFieldBehavior.graphics = bufferedImage.createGraphics().apply {
                setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
                scale(contentScale, contentScale)
            }
        }
    }

    override fun render(view: TextField, canvas: Canvas) {
        if (this::graphics.isInitialized) {
            graphics.background = Color(255, 255, 255, 0)
            graphics.clearRect(0, 0, bufferedImage.width, bufferedImage.height)

            nativePeer.paint(graphics)
            canvas.scale(1 / contentScale, 1 / contentScale) {
                canvas.image(ImageImpl(bufferedImage.toImage(), ""))
            }
        }
    }

    override fun mirrorWhenRightToLeft(view: TextField) = false

    // FIXME: This doesn't return the proper text size
    override fun fitTextSize(textField: TextField) = nativePeer.preferredScrollableViewportSize.run { Size(width, height) }

    override fun install(view: TextField) {
        super.install(view)

        nativePeer = JTextFieldPeer(view)

        createNewBufferedImage(view.size)

        view.apply {
            maskChanged          += this@NativeTextFieldBehavior.maskChanged
            textChanged          += this@NativeTextFieldBehavior.textChanged
            focusChanged         += this@NativeTextFieldBehavior.focusChanged
            styleChanged         += this@NativeTextFieldBehavior.styleChanged
            boundsChanged        += this@NativeTextFieldBehavior.boundsChanged
            enabledChanged       += this@NativeTextFieldBehavior.enabledChanged
            pointerChanged       += this@NativeTextFieldBehavior
            selectionChanged     += this@NativeTextFieldBehavior.selectionChanged
            focusabilityChanged  += this@NativeTextFieldBehavior.focusableChanged
            pointerMotionChanged += this@NativeTextFieldBehavior
        }

        appScope.launch(uiDispatcher) {
            nativePeer.size = view.size.run { Dimension(view.width.toInt(), view.height.toInt()) }

            window.add(nativePeer)
            nativePeer.revalidate()

            view.apply {
                cursor    = Default
                idealSize = nativePeer.preferredSize.run { Size(width, height) }
            }
        }
    }

    override fun uninstall(view: TextField) {
        super.uninstall(view)

        view.apply {
            cursor    = oldCursor
            idealSize = oldIdealSize

            maskChanged          -= this@NativeTextFieldBehavior.maskChanged
            textChanged          -= this@NativeTextFieldBehavior.textChanged
            focusChanged         -= this@NativeTextFieldBehavior.focusChanged
            styleChanged         -= this@NativeTextFieldBehavior.styleChanged
            boundsChanged        -= this@NativeTextFieldBehavior.boundsChanged
            enabledChanged       -= this@NativeTextFieldBehavior.enabledChanged
            pointerChanged       -= this@NativeTextFieldBehavior
            selectionChanged     -= this@NativeTextFieldBehavior.selectionChanged
            focusabilityChanged  -= this@NativeTextFieldBehavior.focusableChanged
            pointerMotionChanged -= this@NativeTextFieldBehavior
        }

        appScope.launch(uiDispatcher) {
            window.remove(nativePeer)
        }
    }

    override fun entered(event: PointerEvent) {
        nativePeer.processMouseEvent(event.toAwt(nativePeer))
    }

    override fun exited(event: PointerEvent) {
        nativePeer.processMouseEvent(event.toAwt(nativePeer))
    }

    override fun pressed(event: PointerEvent) {
        nativePeer.processMouseEvent(event.toAwt(nativePeer))
    }

    override fun released(event: PointerEvent) {
        nativePeer.processMouseEvent(event.toAwt(nativePeer))
    }

    override fun moved(event: PointerEvent) {
        nativePeer.processMouseMotionEvent(event.toAwt(nativePeer))
    }
}