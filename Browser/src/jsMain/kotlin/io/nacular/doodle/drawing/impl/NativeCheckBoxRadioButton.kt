package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ButtonModel
import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Inline
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.insert
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setLeft
import io.nacular.doodle.dom.setMargin
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.setTop
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.text.TextSpacing.Companion.default
import io.nacular.doodle.utils.Anchor.Right
import io.nacular.doodle.utils.Anchor.Trailing
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
internal enum class Type(internal val value: String) { Check("checkbox"), Radio("radio") }

internal interface NativeCheckBoxRadioButtonFactory {
    operator fun invoke(button: Button, type: Type): NativeCheckBoxRadioButton
}

internal class NativeCheckBoxRadioButtonFactoryImpl internal constructor(
        private val textFactory              : TextFactory,
        private val textMetrics              : TextMetrics,
        private val htmlFactory              : HtmlFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?
): NativeCheckBoxRadioButtonFactory {

    override fun invoke(button: Button, type: Type) = NativeCheckBoxRadioButton(
            button,
            textFactory,
            textMetrics,
            elementRuler,
            nativeEventHandlerFactory,
            focusManager,
            htmlFactory,
            type
    )
}

internal class NativeCheckBoxRadioButton(
        private val button            : Button,
        private val textFactory       : TextFactory,
        private val textMetrics       : TextMetrics,
        private val elementRuler      : ElementRuler,
        private val handlerFactory    : NativeEventHandlerFactory,
        private val focusManager      : FocusManager?,
                    htmlFactory       : HtmlFactory,
                    type              : Type
): NativeEventListener {

    private var textSize  = Empty
    private var inputSize = Empty

    val idealSize: Size get() = Size(
        inputSize.width + if (textSize.width > 0) button.iconTextSpacing + textSize.width else 0.0,
        max(inputSize.height, textSize.height)
    )

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
            it.style.fontFamily = "initial"
            it.style.fontSize   = "initial"
            it.style.fontWeight = "initial"

            it.add(this)
        }

        inputSize = elementRuler.size(block)

        style.setPosition() // restore absolute

        inputEventHandler = handlerFactory(this, this@NativeCheckBoxRadioButton).apply {
            registerFocusListener           ()
            startConsumingPointerPressEvents()
        }
    }

    private val rootElement = htmlFactory.create<HTMLElement>("LABEL").apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)

        rootEventHandler = handlerFactory(this, this@NativeCheckBoxRadioButton).apply {
            registerFocusListener           ()
            registerClickListener           ()
            startConsumingPointerPressEvents()
        }

        add(inputElement)
    }

    private var textElement: HTMLElement? = null

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when {
            new  -> inputElement.focus()
            else -> inputElement.blur ()
        }
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

        rootEventHandler.unregisterFocusListener         ()
        rootEventHandler.unregisterClickListener         ()
        rootEventHandler.stopConsumingPointerPressEvents ()

        inputEventHandler.unregisterFocusListener        ()
        inputEventHandler.stopConsumingPointerPressEvents()
    }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(rootElement), Origin)

            positionElements()

            if (button.hasFocus) {
                inputElement.focus()
            }
        }
    }

    override fun onClick(event: Event) = true.also {
        if (isKeyboardClick(event) || event.target is HTMLInputElement) {
            button.click()
        }
    }

    override fun onFocusGained(event: Event): Boolean {
        button.let {
            if (!it.focusable) {
                return false
            }

            focusManager?.requestFocus(it)
        }

        return true
    }

    override fun onFocusLost(event: Event) = true.also {
        if (button === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }
    }

    private var text = button.text
        set(value) {
            field = value

            textElement?.let { rootElement.remove(it) }

            textElement = field.takeIf { it.isNotBlank() }?.let {
                textFactory.create(it, button.font, textSpacing = default).also { text ->
                    rootElement.insert(text, 0)

                    text.style.setMargin (0.0     )
                    text.style.setDisplay(Inline())

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
                Right, Trailing -> {
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