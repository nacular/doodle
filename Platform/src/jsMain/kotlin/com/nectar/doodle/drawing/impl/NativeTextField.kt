package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.text.Selection
import com.nectar.doodle.controls.text.TextField
import com.nectar.doodle.controls.text.TextInput
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.BoxSizing.Border
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setBackgroundColor
import com.nectar.doodle.dom.setBorderWidth
import com.nectar.doodle.dom.setBoxSizing
import com.nectar.doodle.dom.setColor
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setOutlineWidth
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Right
import kotlin.math.max


interface NativeTextFieldFactory {
    operator fun invoke(textField: TextField): NativeTextField
}

class NativeTextFieldFactoryImpl internal constructor(
        private val htmlFactory        : HtmlFactory,
        private val elementRuler       : ElementRuler,
        private val eventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager       : FocusManager?,
        private val textMetrics        : TextMetrics): NativeTextFieldFactory {

    private val sizeDifference: Size by lazy {
        elementRuler.size(htmlFactory.createInput()).let {
            Size(it.width - defaultSize.width, it.height - defaultSize.height)
        }
    }

    private val defaultSize: Size by lazy {
        elementRuler.size(htmlFactory.createInput().apply {
            style.setBorderWidth (0.0)
            style.setOutlineWidth(0.0)
        })
    }

    override fun invoke(textField: TextField) = NativeTextField(
            eventHandlerFactory,
            htmlFactory,
            focusManager,
            textMetrics,
            sizeDifference,
            textField)
}

@Suppress("PrivatePropertyName")
class NativeTextField(
                    eventHandlerFactory: NativeEventHandlerFactory,
                    htmlFactory        : HtmlFactory,
        private val focusManager       : FocusManager?,
        private val textMetrics        : TextMetrics,
        private val borderSize         : Size,
        private val textField          : TextField): NativeEventListener {

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
        when (new) {
            true -> inputElement.focus()
            else -> inputElement.blur ()
        }
    }

    private val enabledChanged = { _: View, _: Boolean, new: Boolean ->
        inputElement.disabled = !new
    }

    private val styleChanged = { _: View ->
        inputElement.style.setColor          (textField.foregroundColor)
        inputElement.style.setBackgroundColor(textField.backgroundColor)
        inputElement.style.textAlign = when (textField.horizontalAlignment) {
            Center -> "center"
            Right  -> "right"
            else   -> ""
        }
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

        inputElement.apply {
            style.setBoxSizing      (Border()                 )
            if (!textField.borderVisible) {
                style.setBorderWidth (0.0                     )
                style.setOutlineWidth(0.0                     )
            }
            style.setWidthPercent   (100.0                    )
            style.setHeightPercent  (100.0                    )
            style.setColor          (textField.foregroundColor)
            style.setBackgroundColor(textField.backgroundColor)
            style.textAlign = when (textField.horizontalAlignment) {
                Center -> "center"
                Right  -> "right"
                else   -> ""
            }

            type       = if (textField.masked) "password" else "text"
            spellcheck = false
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
    }

    fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {
            canvas.addData(listOf(inputElement))
        }

        if (textField.hasFocus) {
            inputElement.focus()
        }

        select(textField.selection.run { start .. end })
    }

    fun fitTextSize() = textMetrics.size(textField.displayText).run {
        Size(max(8.0, width) + 1.6 + if (textField.borderVisible) borderSize.width else 0.0, height + if (textField.borderVisible) borderSize.height else 0.0)
    }

    override fun onKeyUp() = true.also { syncTextField() }
    override fun onInput() = true.also { syncTextField() }

    override fun onFocusGained() = true.also {
        if (!ignoreSync) {
            focusManager?.requestFocus(textField)
        }
    }

    override fun onFocusLost() = true.also {
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