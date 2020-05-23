package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.ButtonModel
import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Inline
import com.nectar.doodle.dom.Static
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setLeft
import com.nectar.doodle.dom.setMargin
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.dom.setTop
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.utils.Anchor
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventTarget
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
enum class Type(internal val value: String) { Check("checkbox"), Radio("radio") }

internal interface NativeCheckBoxRadioButtonFactory {
    operator fun invoke(button: Button, type: Type): NativeCheckBoxRadioButton
}

internal class NativeCheckBoxRadioButtonFactoryImpl internal constructor(
        private val textFactory              : TextFactory,
        private val textMetrics              : TextMetrics,
        private val htmlFactory              : HtmlFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?): NativeCheckBoxRadioButtonFactory {
    override fun invoke(button: Button, type: Type) = NativeCheckBoxRadioButton(
            button,
            textFactory,
            textMetrics,
            elementRuler,
            nativeEventHandlerFactory,
            focusManager,
            htmlFactory,
            type)
}

internal class NativeCheckBoxRadioButton(
        private val button        : Button,
        private val textFactory   : TextFactory,
        private val textMetrics   : TextMetrics,
        private val elementRuler  : ElementRuler,
        private val handlerFactory: NativeEventHandlerFactory,
        private val focusManager  : FocusManager? = null,
                    htmlFactory   : HtmlFactory,
                    type          : Type): NativeEventListener {

    private var textSize  = Empty
    private var inputSize = Empty

    val idealSize: Size? get() {
        return Size(inputSize.width + if (textSize.width > 0) button.iconTextSpacing + textSize.width else 0.0, max(inputSize.height, textSize.height))
    }

    private val rootEventHandler : NativeEventHandler
    private val inputEventHandler: NativeEventHandler

    private val inputElement = htmlFactory.createInput().apply {
        this.type = type.value
        checked   = button.selected
        tabIndex  = if (button.selected) 0 else -1
        disabled  = !button.enabled

        if (button is CheckBox) {
            indeterminate = button.indeterminate
        }

        style.setPosition(Static()) // remove absolute positioning due to global style

        val block = htmlFactory.create<HTMLElement>().also {
            it.style.setPosition(Static())
            it.style.fontFamily = "unset"
            it.style.fontSize   = "unset"

            it.add(this)
        }

        inputSize = elementRuler.size(block)

        style.setPosition() // restore absolute

        inputEventHandler = handlerFactory(this, this@NativeCheckBoxRadioButton).apply {
            registerFocusListener         ()
            startConsumingPointerPressEvents()
        }
    }

    private val rootElement = htmlFactory.create<HTMLElement>("LABEL").apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)

        rootEventHandler = handlerFactory(this, this@NativeCheckBoxRadioButton).apply {
            registerFocusListener         ()
            registerClickListener         ()
            startConsumingPointerPressEvents()
        }

        add(inputElement)
    }

    private var textElement: HTMLElement? = null

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        if (new) inputElement.focus() else inputElement.blur()
    }

    private val styleChanged: (View) -> Unit = {
        if (button is CheckBox) {
            inputElement.indeterminate = button.indeterminate
        }

        it.rerender()
    }

    private val selectionChanged: (ButtonModel, Boolean, Boolean) -> Unit = { _,_,new ->
        inputElement.checked = new
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        inputElement.disabled = !new
    }

    private val focusabilityChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        inputElement.tabIndex = if (new) 0 else -1
    }

    init {
        button.apply {
            focusChanged          += this@NativeCheckBoxRadioButton.focusChanged
            styleChanged          += this@NativeCheckBoxRadioButton.styleChanged
            enabledChanged        += this@NativeCheckBoxRadioButton.enabledChanged
            focusabilityChanged   += this@NativeCheckBoxRadioButton.focusabilityChanged
            model.selectedChanged += this@NativeCheckBoxRadioButton.selectionChanged
        }

        setText()
    }

    fun discard() {
        button.apply {
            focusChanged          -= this@NativeCheckBoxRadioButton.focusChanged
            styleChanged          -= this@NativeCheckBoxRadioButton.styleChanged
            enabledChanged        -= this@NativeCheckBoxRadioButton.enabledChanged
            focusabilityChanged   -= this@NativeCheckBoxRadioButton.focusabilityChanged
            model.selectedChanged -= this@NativeCheckBoxRadioButton.selectionChanged
        }

        rootEventHandler.unregisterFocusListener       ()
        rootEventHandler.stopConsumingPointerPressEvents ()
        inputEventHandler.unregisterFocusListener      ()
        inputEventHandler.unregisterClickListener      ()
        inputEventHandler.stopConsumingPointerPressEvents()
    }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(rootElement), Point.Origin)

            positionElements()

            if (button.hasFocus) {
                inputElement.focus()
            }
        }
    }

    override fun onClick(target: EventTarget?) = true.also {
        button.click()
    }

    override fun onFocusGained(target: EventTarget?): Boolean {
        button.let {
            if (!it.focusable) {
                return false
            }

            focusManager?.requestFocus(it)
        }

        return true
    }

    override fun onFocusLost(target: EventTarget?) = true.also {
        if (button === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }
    }

    private var text = button.text
        set(value) {
            field = value

            textElement?.let { rootElement.remove(it) }

            textElement = field.takeIf { it.isNotBlank() }?.let {
                textFactory.create(it, button.font).also {
                    rootElement.insert(it, 0)

                    it.style.setMargin (0.0     )
                    it.style.setDisplay(Inline())

                    textSize = textMetrics.size(button.text, button.font)
                }
            }
        }

    private fun setText() {
        text = button.text
    }

    private fun positionElements() {
        inputElement.style.setTop ((button.height - inputSize.height) / 2)

        textElement?.let { textElement ->
            textElement.style.setTop((button.height - textSize.height ) / 2)

            when (button.iconAnchor) {
                Anchor.Right, Anchor.Trailing -> {
                    rootElement.insert(textElement, 0)

                    inputElement.style.setLeft(textSize.width + button.iconTextSpacing)
                    textElement.style.setLeft(0.0)
                }

                else                          -> {
                    rootElement.add(textElement)
                    inputElement.style.setLeft(0.0)
                    textElement.style.setLeft(inputSize.width + button.iconTextSpacing)
                }
            }
        }
    }
}
