package io.nacular.doodle.drawing.impl

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.controls.text.Selection
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextInput
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.dom.SystemStyler.Style
import io.nacular.doodle.dom.rgba
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setBorderWidth
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOutlineWidth
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.IdGenerator
import org.w3c.dom.events.EventTarget
import kotlin.math.max


internal interface NativeTextFieldFactory {
    operator fun invoke(textField: TextField): NativeTextField
}

internal class NativeTextFieldFactoryImpl internal constructor(
        private val idGenerator        : IdGenerator,
        private val fontSerializer     : FontSerializer,
        private val systemStyler       : SystemStyler,
        private val htmlFactory        : HtmlFactory,
        private val elementRuler       : ElementRuler,
        private val eventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager       : FocusManager?,
        private val textMetrics        : TextMetrics,
        private val spellCheck         : Boolean): NativeTextFieldFactory {

    private val sizeDifference: Size by lazy {
        elementRuler.size(htmlFactory.createInput()).let {
            Size(it.width - defaultSize.width, it.height - defaultSize.height)
        }
    }

    private val defaultSize: Size by lazy {
        elementRuler.size(htmlFactory.createInput().apply {
            style.setBorderWidth (0.0)
            style.setOutlineWidth(0.0)
            style.padding = "0px"
        })
    }

    override fun invoke(textField: TextField) = NativeTextField(
            eventHandlerFactory,
            idGenerator,
            systemStyler,
            fontSerializer,
            htmlFactory,
            focusManager,
            textMetrics,
            sizeDifference,
            spellCheck,
            textField)
}

@Suppress("PrivatePropertyName")
internal class NativeTextField(
                    eventHandlerFactory: NativeEventHandlerFactory,
        private val idGenerator        : IdGenerator,
        private val systemStyler       : SystemStyler,
        private val fontSerializer     : FontSerializer,
                    htmlFactory        : HtmlFactory,
        private val focusManager       : FocusManager?,
        private val textMetrics        : TextMetrics,
        private val borderSize         : Size,
        private val spellCheck         : Boolean,
        private val textField          : TextField): NativeEventListener {

    val clipCanvasToBounds = false

    var text
        get(   ) = inputElement.value
        set(new) {
            inputElement.value = new
        }

    private val selection
        get() = (inputElement.selectionStart ?: 0) .. (inputElement.selectionEnd ?: 0)

    var size = Empty

    private var ignoreSync   = false
    private val inputElement = htmlFactory.createInput()
    private val eventHandler: NativeEventHandler

    private val textChanged = { _: TextInput, _: String, new: String ->
        text = new
    }

    private val maskChanged = { _: TextInput, _: Char?, new: Char? ->
        inputElement.type = when (new) {
            null -> "text"
            else -> "password"
        }
    }

    private val focusChanged = { _: View, _: Boolean, new: Boolean ->
        ignoreSync = true

        when (new) {
            true -> inputElement.focus()
            else -> inputElement.blur ()
        }

        ignoreSync = false
    }

    private val enabledChanged = { _: View, _: Boolean, new: Boolean ->
        inputElement.disabled = !new
    }

    private var selectionStyle   : Style? by cssStyle()
    private var placeHolderStyle : Style? by cssStyle()
    private var mozSelectionStyle: Style? by cssStyle()

    private fun ensureId() {
        if (inputElement.id == "") {
            inputElement.id = idGenerator.nextId()
        }
    }

    private val styleChanged = { _: View ->
        inputElement.run {
            placeholder = textField.placeHolder

            if (textField.placeHolderColor ?: textField.placeHolderFont != null) {
                ensureId()
                val css = """#$id::placeholder {
                    |${textField.placeHolderColor?.let { "color:${rgba(it)};" } ?: ""}
                    |${textField.placeHolderFont?.let  { "font:${fontSerializer(it)};" } ?: ""}
                    |line-height:inherit;
                |}""".trimMargin()
                this@NativeTextField.placeHolderStyle = systemStyler.insertRule(css)
            }

            if (textField.selectionForegroundColor != null || textField.selectionBackgroundColor != null) {
                ensureId()
                val styleBody = """
                    |${textField.selectionForegroundColor?.let { "color:${rgba(it)};" } ?: ""}
                    |${textField.selectionBackgroundColor?.let { "background-color:${rgba(it)};" }}
                """.trimMargin()

                this@NativeTextField.mozSelectionStyle = systemStyler.insertRule("#$id::-moz-selection {$styleBody}")
                this@NativeTextField.selectionStyle    = systemStyler.insertRule("#$id::selection {$styleBody}")
            }

            style.setFont(textField.font)
            style.setColor(textField.foregroundColor)
            style.setBackgroundColor(textField.backgroundColor)
            style.textAlign = when (textField.horizontalAlignment) {
                Center -> "center"
                Right  -> "right"
                else   -> ""
            }

            style.outline = if (textField.borderVisible) "" else "none"
            style.setBorderWidth (if (textField.borderVisible) null else 0.0)
            style.setOutlineWidth(if (textField.borderVisible) null else 0.0)
        }
    }

    private val boundsChanged = { _: View, _: Rectangle, new: Rectangle ->
        inputElement.style.lineHeight = "${new.height}px"
    }

    private val focusabilityChanged = { _: View, _: Boolean, new: Boolean ->
        inputElement.tabIndex = if (new) -1 else 0
    }

    private val selectionChanged = { _: TextInput, _: Selection, new: Selection ->
        ignoreSync = true

        select(new.start .. new.end)

        ignoreSync = false
    }

    init {
        text = textField.text

        styleChanged(textField)

        inputElement.apply {
            style.setWidthPercent (100.0)
            style.setHeightPercent(100.0)
            style.lineHeight = "${textField.height}px"

            type         = if (textField.masked) "password" else "text"
            spellcheck   = spellCheck
        }

        eventHandler = eventHandlerFactory(inputElement, this).apply {
            registerKeyListener  ()
            registerFocusListener()
            registerClickListener()
            registerInputListener()
        }

        textField.apply {
            textChanged         += this@NativeTextField.textChanged
            maskChanged         += this@NativeTextField.maskChanged
            focusChanged        += this@NativeTextField.focusChanged
            styleChanged        += this@NativeTextField.styleChanged
            boundsChanged       += this@NativeTextField.boundsChanged
            enabledChanged      += this@NativeTextField.enabledChanged
            selectionChanged    += this@NativeTextField.selectionChanged
            focusabilityChanged += this@NativeTextField.focusabilityChanged
        }
    }

    fun discard() {
        textField.apply {
            textChanged         -= this@NativeTextField.textChanged
            maskChanged         -= this@NativeTextField.maskChanged
            focusChanged        -= this@NativeTextField.focusChanged
            styleChanged        -= this@NativeTextField.styleChanged
            enabledChanged      -= this@NativeTextField.enabledChanged
            selectionChanged    -= this@NativeTextField.selectionChanged
            focusabilityChanged -= this@NativeTextField.focusabilityChanged
        }

        selectionStyle    = null
        placeHolderStyle  = null
        mozSelectionStyle = null
    }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(inputElement))
        }

        if (textField.hasFocus) {
            inputElement.focus()
        }

        select(textField.selection.run { start .. end })
    }

    fun fitTextSize() = textMetrics.size(textField.displayText, textField.font).run {
        val h = if (height == 0.0) textField.height - borderSize.height else height

        Size(max(8.0, width) + borderSize.width, h + borderSize.height)
    }

    override fun onKeyUp(target: EventTarget?) = true.also { syncTextField() }
    override fun onInput(target: EventTarget?) = true.also { syncTextField() }

    override fun onFocusGained(target: EventTarget?) = true.also {
        if (!ignoreSync) {
            focusManager?.requestFocus(textField)
        }
    }

    override fun onFocusLost(target: EventTarget?) = true.also {
        if (!ignoreSync && focusManager?.focusOwner == textField) {
            focusManager.clearFocus()
        }
    }

    private fun select(range: ClosedRange<Int>) = inputElement.setSelectionRange(range.start, range.endInclusive)

    private fun syncTextField() {
        if (ignoreSync) {
            return
        }

        // Ignore updates from the TextField during this call since
        // they will invalidate the underlying selection range
        // by modifying the start/end position.  They are also
        // redundant.

        textField.textChanged      -= textChanged
        textField.selectionChanged -= selectionChanged

        textField.text = text

        textField.select(selection)

        textField.textChanged      += textChanged
        textField.selectionChanged += selectionChanged
    }
}