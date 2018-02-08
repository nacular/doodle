package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.text.Selection
import com.nectar.doodle.controls.text.TextInput
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.dom.BoxSizing.Border
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setBoxSizing
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.zinoti.jaz.controls.text.TextField


interface NativeTextFieldFactory {
    operator fun invoke(textField: TextField): NativeTextField
}

class NativeTextFieldFactoryImpl internal constructor(
        private val htmlFactory        : HtmlFactory,
        private val eventHandlerFactory: NativeEventHandlerFactory): NativeTextFieldFactory {
    override fun invoke(textField: TextField) = NativeTextField(
            eventHandlerFactory,
            htmlFactory,
            textField)
}

class NativeTextField(
        private val eventHandlerFactory: NativeEventHandlerFactory,
        htmlFactory: HtmlFactory,
        private val textField: TextField): NativeEventListener {

    var text: String
        get(   ) = inputElement.value
        set(new) {
            inputElement.value = new
        }

    val selection: ClosedRange<Int>
        get() = (inputElement.selectionStart ?: 0) .. (inputElement.selectionEnd ?: 0)

    var size = Empty

    var enabled get() = !inputElement.disabled
        set(new) {
            inputElement.disabled = !new
        }

    private var ignoreSync   = false
    private val inputElement = htmlFactory.createInput()
    private val eventHandler: NativeEventHandler

    init {
        text = textField.text

        inputElement.apply {
            style.setWidthPercent (100.0)
            style.setHeightPercent(100.0)
            style.setBoxSizing(Border)
        }

        eventHandler = eventHandlerFactory(inputElement, this).apply {
            registerKeyListener  ()
            registerFocusListener()
            registerClickListener()
        }

        textField.apply {
            textChanged      += ::textChanged
            focusChanged     += ::focusChanged
            enabledChanged   += ::enabledChanged
            focusableChanged += ::focusableChanged
            selectionChanged += ::selectionChanged
        }
    }

    fun discard() {
        textField.apply {
            textChanged      -= ::textChanged
            focusChanged     -= ::focusChanged
            enabledChanged   -= ::enabledChanged
            focusableChanged -= ::focusableChanged
            selectionChanged -= ::selectionChanged
        }
    }

    fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {
            canvas.addData(listOf(inputElement))
        }
    }

    override fun onKeyUp(): Boolean {
        syncTextField()
        return true
    }

    override fun onKeyDown(): Boolean {
        syncTextField()
        return true
    }

    override fun onKeyPress(): Boolean {
        syncTextField()
        return true
    }

    override fun onFocusGained(): Boolean {
        if (!ignoreSync) {
//            Service.locator().getFocusManager().requestFocus(textField)
        }
        return true
    }

    override fun onFocusLost(): Boolean {
        if (!ignoreSync) {
//            Service.locator().getFocusManager().requestFocus(null)
        }
        return true
    }


    private fun select(range: ClosedRange<Int>) = inputElement.setSelectionRange(range.start, range.endInclusive)

    @Suppress("UNUSED_PARAMETER")
    private fun selectionChanged(textInput: TextInput, old: Selection, new: Selection) {
        ignoreSync = true

        select(new.start .. new.end)

        ignoreSync = false
    }

    @Suppress("UNUSED_PARAMETER")
    private fun textChanged(textInput: TextInput, old: String, new: String) {
        text = new
    }

    @Suppress("UNUSED_PARAMETER")
    private fun focusChanged(gizmo: Gizmo, old: Boolean, new: Boolean) {
        when (new) {
            true -> inputElement.focus()
            else -> inputElement.blur ()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun enabledChanged(gizmo: Gizmo, old: Boolean, new: Boolean) {
        inputElement.disabled = !new
    }

    @Suppress("UNUSED_PARAMETER")
    private fun focusableChanged(gizmo: Gizmo, old: Boolean, new: Boolean) {
        inputElement.tabIndex = if (new) -1 else 0
    }

    private fun syncTextField() {
        if (ignoreSync) {
            return
        }

        // Ignore updates from the TextField during this call since
        // they will invalidate the underlying selection range
        // by modifying the start/end position.  They are also
        // redundant.

        textField.textChanged      -= ::textChanged
        textField.selectionChanged -= ::selectionChanged

        textField.text = text

        textField.select(selection)

        textField.textChanged      += ::textChanged
        textField.selectionChanged += ::selectionChanged
    }
}