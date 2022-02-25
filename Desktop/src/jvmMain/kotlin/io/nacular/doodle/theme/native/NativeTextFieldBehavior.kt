package io.nacular.doodle.theme.native

import io.nacular.doodle.controls.text.Selection
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextFieldBehavior
import io.nacular.doodle.controls.text.TextInput
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.CanvasImpl
import io.nacular.doodle.drawing.impl.TextMetricsImpl
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.Cursor.Companion.Default
import io.nacular.doodle.system.SystemPointerEvent.Type.*
import io.nacular.doodle.utils.HorizontalAlignment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.Typeface
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent
import kotlin.coroutines.CoroutineContext
import org.jetbrains.skia.Font as SkiaFont

/**
 * Behavior that modifies the background and foreground of a [TextField].
 */
public interface NativeTextFieldBehaviorModifier: Behavior<TextField> {
    /**
     * Allows custom rendering for [textField]'s background
     * NOTE: implementations should most likely update [TextField.backgroundColor] to
     * ensure the results of this call are visible.
     *
     * @param textField being rendered
     * @param canvas to render onto
     */
    public fun renderBackground(textField: TextField, canvas: Canvas) {}

    /**
     * Allows custom rendering for [textField]'s foreground.
     *
     * @param textField being rendered
     * @param canvas to render onto
     */
    public fun renderForeground(textField: TextField, canvas: Canvas) {}
}

/**
 * Allows more control over how native [TextField]s are styled. The given behavior is delegated
 * to for all visual styling, and has the ability to render any background or foreground along with
 * the browser's layer.
 */
public interface NativeTextFieldBehaviorModifierBuilder {
    /**
     * Wraps [behavior] with other native styling for text fields.
     *
     * @param textField to apply [behavior] to
     * @param behavior to be "wrapped"
     * @return a new Behavior for the text field
     */
    public operator fun invoke(textField: TextField, behavior: NativeTextFieldBehaviorModifier): TextFieldBehavior
}

internal class NativeTextFieldStylerImpl(
        private val window              : JPanel,
        private val appScope            : CoroutineScope,
        private val uiDispatcher        : CoroutineContext,
        private val defaultFont         : SkiaFont,
        private val swingGraphicsFactory: SwingGraphicsFactory,
        private val swingFocusManager   : javax.swing.FocusManager,
        private val textMetrics         : TextMetricsImpl,
        private val focusManager        : FocusManager?
): NativeTextFieldStyler {
    override fun invoke(textField: TextField, behavior: NativeTextFieldBehaviorModifier): TextFieldBehavior = NativeTextFieldBehaviorWrapper(
            window,
            appScope,
            uiDispatcher,
            defaultFont,
            swingGraphicsFactory,
            swingFocusManager,
            textMetrics,
            focusManager,
            behavior
    )
}

private class NativeTextFieldBehaviorWrapper(
                    window              : JPanel,
                    appScope            : CoroutineScope,
                    uiDispatcher        : CoroutineContext,
                    defaultFont         : SkiaFont,
                    swingGraphicsFactory: SwingGraphicsFactory,
                    swingFocusManager   : javax.swing.FocusManager,
                    textMetrics         : TextMetricsImpl,
                    focusManager        : FocusManager?,
        private val delegate            : NativeTextFieldBehaviorModifier): NativeTextFieldBehavior(
        window,
        appScope,
        uiDispatcher,
        defaultFont,
        swingGraphicsFactory,
        swingFocusManager,
        textMetrics,
        focusManager), Behavior<TextField> by delegate {

    override fun render(view: TextField, canvas: Canvas) {
        delegate.renderBackground(view, canvas)
        super<NativeTextFieldBehavior>.render(view, canvas)
        delegate.renderForeground(view, canvas)
    }

    override fun fitTextSize(textField: TextField): Size = super.fitTextSize(textField)

    override fun install(view: TextField) {
        super<NativeTextFieldBehavior>.install(view)
        delegate.install(view)
    }

    override fun uninstall(view: TextField) {
        super<NativeTextFieldBehavior>.uninstall(view)
        delegate.uninstall(view)
    }
}

private class PlaceHolderLabel(text: String, private val component: JTextComponent): JLabel(), DocumentListener {
    private fun updateVisibility() {
        isVisible = component.document.length == 0
    }

    override fun insertUpdate (e: DocumentEvent) = updateVisibility()
    override fun removeUpdate (e: DocumentEvent) = updateVisibility()
    override fun changedUpdate(e: DocumentEvent) = updateVisibility()

    init {
        border              = null
        this.text           = text
        foreground          = component.foreground
        horizontalAlignment = LEADING

        updateVisibility()

        component.layout = BorderLayout()
        component.document.addDocumentListener(this)
        component.add(this)
    }

    fun dispose() {
        component.document.removeDocumentListener(this)
        component.remove(this)
    }
}

internal open class NativeTextFieldBehavior(
        private val window              : JPanel,
        private val appScope            : CoroutineScope,
        private val uiDispatcher        : CoroutineContext,
        private val defaultFont         : SkiaFont,
        private val swingGraphicsFactory: SwingGraphicsFactory,
        private val swingFocusManager   : javax.swing.FocusManager,
        private val textMetrics         : TextMetricsImpl,
        private val focusManager        : FocusManager?
): TextFieldBehavior, PointerListener, PointerMotionListener {

    private inner class JTextFieldPeer(textField: TextField): JPasswordField() {
        private val textField: TextField? = textField

        var placeHolder: String
            get()    = placeHolderLabel?.text ?: ""
            set(new) {
                placeHolderLabel?.dispose()
                placeHolderLabel = when (new) {
                    "" -> null
                    else -> PlaceHolderLabel(new, this).apply {
                        (textField?.placeHolderFont ?: textField?.font).toAwt(defaultFont).let { font       = it }
                        textField?.placeHolderColor?.toAwt                   (           ).let { foreground = it }

                        horizontalAlignment = when (textField?.horizontalAlignment) {
                            Center -> CENTER
                            Right  -> RIGHT
                            else   -> LEADING
                        }
                    }
                }
            }

        private var placeHolderLabel: PlaceHolderLabel? = null
        private var defaultBorder = border

        init {
            text                      = textField.text
            focusTraversalKeysEnabled = false

            stylesChanged()

            addComponentListener(object : ComponentAdapter() {
                override fun componentShown(e: ComponentEvent) {
                    revalidate()
                }
            })

            addFocusListener(object : FocusListener {
                override fun focusGained(e: FocusEvent?) {
                    if (textField != focusManager?.focusOwner) {
                        focusManager?.requestFocus(textField)
                    }
                }

                override fun focusLost(e: FocusEvent?) {
                    if (textField == focusManager?.focusOwner) {
                        focusManager.clearFocus()
                    }
                }
            })

            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate (e: DocumentEvent?) = syncTextFromSwing()
                override fun removeUpdate (e: DocumentEvent?) = syncTextFromSwing()
                override fun changedUpdate(e: DocumentEvent?) = syncTextFromSwing()
            })
        }

        override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {
            textField?.rerender()
        }

        override fun getFontMetrics(font: Font?): FontMetrics = font?.let {
            SkiaFontMetrics(SkiaFont(Typeface.makeFromName(font.family, font.skiaStyle()), font.size.toFloat()), font, textMetrics)
        } ?: super.getFontMetrics(font)

        public override fun processMouseEvent(e: MouseEvent?) {
            super.processMouseEvent(e)
        }

        public override fun processMouseMotionEvent(e: MouseEvent?) {
            super.processMouseMotionEvent(e)
        }

        fun stylesChanged() {
            if (textField == null) return

            echoChar    = textField.mask ?: 0.toChar()
            placeHolder = textField.placeHolder

            textField.font.toAwt(defaultFont).let            { font              = it }
            textField.foregroundColor?.toAwt().let           { foreground        = it }
            textField.backgroundColor?.toAwt()?.let          { background        = it }
            textField.selectionForegroundColor?.toAwt()?.let { selectedTextColor = it }
            textField.selectionBackgroundColor?.toAwt()?.let { selectionColor    = it }
            textField.backgroundColor?.takeIf { it.opacity != 1f }.let { isOpaque = false }

            horizontalAlignment = when (textField.horizontalAlignment) {
                Center -> CENTER
                Right  -> RIGHT
                else   -> LEADING
            }

            border = when {
                textField.borderVisible -> defaultBorder
                else                    -> null
            }
        }

        private fun syncTextFromSwing() {
            ignoreDoodleTextChange = true
            textField?.text = password.concatToString()
            ignoreDoodleTextChange = false
        }
    }

    private          var oldCursor    : Cursor? = null
    private lateinit var nativePeer   : JTextFieldPeer
    private          var oldIdealSize : Size?   = null
    private          var ignoreDoodleTextChange = false

    private val maskChanged = { _: TextField, _: Char?, new: Char? ->
        nativePeer.echoChar = new ?: 0.toChar()
    }

    private val textChanged: (TextInput, String, String) -> Unit = { _, _, new ->
        if (!ignoreDoodleTextChange) {
            nativePeer.text = new
        }
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        when (new) {
            true -> if (!nativePeer.hasFocus()) {
                nativePeer.requestFocusInWindow()
            }
            else -> if ( nativePeer.hasFocus()) { swingFocusManager.clearFocusOwner() }
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        nativePeer.isEnabled = new
    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _, _, new ->
        nativePeer.isFocusable = new
    }

    private val styleChanged: (source: View) -> Unit = {
        (it as? TextField)?.let { _ ->
            nativePeer.stylesChanged()
        }
    }

    private val selectionChanged: (source: TextInput, old: Selection, new: Selection) -> Unit = { _, _, new ->
        nativePeer.select(new.start, new.end)
    }

    private val boundsChanged: (View, Rectangle, Rectangle) -> Unit = { _, _, new ->
        nativePeer.size = new.size.run { Dimension(width.toInt(), height.toInt()) }
        nativePeer.revalidate()
    }

    override fun render(view: TextField, canvas: Canvas) {
        nativePeer.paint(swingGraphicsFactory((canvas as CanvasImpl).skiaCanvas))
    }

    override fun mirrorWhenRightToLeft(view: TextField) = false

    // FIXME: This doesn't return the proper text size
    override fun fitTextSize(textField: TextField) = nativePeer.preferredScrollableViewportSize.run { Size(width, height) }

    override fun install(view: TextField) {
        super.install(view)

        nativePeer = JTextFieldPeer(view)

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

            if (view.hasFocus) {
                nativePeer.requestFocusInWindow()
            }

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

    override fun dragged(event: PointerEvent) {
        nativePeer.processMouseMotionEvent(event.toAwt(nativePeer))
    }
}