package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.text.SelectionEvent
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.dom.BoxSizing.Border
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setBoxSizing
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.zinoti.jaz.controls.text.TextField
import kotlin.math.max
import kotlin.math.min


interface NativeTextFieldFactory {
    operator fun invoke(textField: TextField): NativeTextField
}

class NativeTextFieldFactoryImpl internal constructor(
        private val htmlFactory           : HtmlFactory,
        private val nativeEventHandler    : () -> NativeEventHandler): NativeTextFieldFactory {
    override fun invoke(textField: TextField) = NativeTextField(
            nativeEventHandler(),
            htmlFactory,
            textField)
}

class NativeTextField(
        private val eventHandler: NativeEventHandler,
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

    init {
        text = textField.text

        inputElement.apply {
            style.setWidthPercent (100.0)
            style.setHeightPercent(100.0)
            style.setBoxSizing(Border)
        }

        eventHandler.registerKeyListener  (inputElement)
        eventHandler.registerFocusListener(inputElement)
        eventHandler.registerClickListener(inputElement)
        eventHandler += this

        textField.apply {
            textChanged      += ::textChanged
            focusChanged     += ::focusChanged
            enabledChanged   += ::enabledChanged
            focusableChanged += ::focusableChanged

            this += ::selectionChanged
        }
    }

    fun discard() {
        textField.apply {
            textChanged      -= ::textChanged
            focusChanged     -= ::focusChanged
            enabledChanged   -= ::enabledChanged
            focusableChanged -= ::focusableChanged
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

    private fun selectionChanged(event: SelectionEvent) {
        val start = min(event.newPosition, event.newAnchor)
        val end   = max(event.newPosition, event.newAnchor)

        ignoreSync = true

        select(start .. end)

        ignoreSync = false
    }

    @Suppress("UNUSED_PARAMETER")
    private fun textChanged(gizmo: Gizmo, old: String, new: String) {
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

        textField.textChanged -= ::textChanged
        textField             -= ::selectionChanged

        textField.text = text

        textField.select(selection)

        textField.textChanged += ::textChanged
        textField             += ::selectionChanged
    }
}