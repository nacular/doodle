package io.nacular.doodle.drawing.impl

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.accessibility.AccessibilityManagerImpl
import io.nacular.doodle.controls.text.Selection
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextField.Purpose
import io.nacular.doodle.controls.text.TextField.Purpose.Email
import io.nacular.doodle.controls.text.TextField.Purpose.Integer
import io.nacular.doodle.controls.text.TextField.Purpose.Number
import io.nacular.doodle.controls.text.TextField.Purpose.Password
import io.nacular.doodle.controls.text.TextField.Purpose.Search
import io.nacular.doodle.controls.text.TextField.Purpose.Telephone
import io.nacular.doodle.controls.text.TextField.Purpose.Url
import io.nacular.doodle.controls.text.TextInput
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HTMLInputElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.dom.SystemStyler.Style
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.caretColor
import io.nacular.doodle.dom.cssStyle
import io.nacular.doodle.dom.focusInput
import io.nacular.doodle.dom.rgba
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setBorderWidth
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setHeight
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOpacity
import io.nacular.doodle.dom.setOutlineWidth
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.IdGenerator
import kotlin.math.max


internal interface NativeTextFieldFactory {
    operator fun invoke(textField: TextField): NativeTextField
}

internal interface MobileKeyboardManager {
    fun show()
    fun hide()
}

internal class NativeTextFieldFactoryImpl internal constructor(
    private val idGenerator         : IdGenerator,
    private val fontSerializer      : FontSerializer,
    private val systemStyler        : SystemStyler,
    private val htmlFactory         : HtmlFactory,
    private val elementRuler        : ElementRuler,
    private val eventHandlerFactory : NativeEventHandlerFactory,
    private val focusManager        : FocusManager?,
    private val textMetrics         : TextMetrics,
    private val accessibilityManager: AccessibilityManagerImpl?,
    private val scheduler           : Scheduler,
    private val spellCheck          : Boolean,
    private val autoComplete        : Boolean
): NativeTextFieldFactory {

    private inner class MobileKeyboardManagerImpl: MobileKeyboardManager {
        private val tempFocusTarget: HTMLInputElement = htmlFactory.createInput().apply {
            setAttribute("type", "text")
            style.setOpacity(0f)
            style.setHeight(0.0)
        }

        override fun show() {
            htmlFactory.root.add(tempFocusTarget)
            tempFocusTarget.focus()
        }

        override fun hide() {
            tempFocusTarget.remove()
        }
    }

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
        eventHandlerFactory   = eventHandlerFactory,
        idGenerator           = idGenerator,
        systemStyler          = systemStyler,
        fontSerializer        = fontSerializer,
        htmlFactory           = htmlFactory,
        focusManager          = focusManager,
        textMetrics           = textMetrics,
        mobileKeyboardManager = MobileKeyboardManagerImpl(),
        accessibilityManager  = accessibilityManager,
        scheduler             = scheduler,
        borderSize            = sizeDifference,
        defaultSize           = defaultSize,
        spellCheck            = spellCheck,
        autoComplete          = autoComplete,
        textField             = textField
    )
}

internal class NativeTextField(
                eventHandlerFactory  : NativeEventHandlerFactory,
    private val idGenerator          : IdGenerator,
    private val systemStyler         : SystemStyler,
    private val fontSerializer       : FontSerializer,
                htmlFactory          : HtmlFactory,
    private val focusManager         : FocusManager?,
    private val textMetrics          : TextMetrics,
    private val mobileKeyboardManager: MobileKeyboardManager,
    private val accessibilityManager : AccessibilityManagerImpl?,
    private val scheduler            : Scheduler,
    private val borderSize           : Size,
    private val defaultSize          : Size,
    private val spellCheck           : Boolean,
    private val autoComplete         : Boolean,
    private val textField            : TextField): NativeEventListener {

    private val inputElement = htmlFactory.createInput().apply {
        if (!autoComplete) {
            this.setAttribute("autocomplete", "off")
        }
    }

    val clipCanvasToBounds = false

    var text by inputElement::value

    var size = Empty

    val preferredSize: Size get() = Size(defaultSize.width, (textField.font?.size?.toDouble() ?: defaultSize.height) + 2 * borderSize.height + 4)

    private val selection get() = (inputElement.selectionStart ?: 0) .. (inputElement.selectionEnd ?: 0)

    private var ignoreSync     = false
    private val eventHandler   : NativeEventHandler
    private var elementFocused = false

    private val textChanged = { _: TextInput, _: String, _: String ->
        text = textField.text
    }

    private val maskChanged = { _: TextInput, _: Char?, new: Char? ->
        when (new) {
            null -> updatePurpose(textField)
            else -> inputElement.type = PASSWORD // force hide
        }
    }

    private val purposeChanged = { _: TextField, _: Purpose, _: Purpose ->
        updatePurpose(textField)
    }

    private val focusChanged = { _: View, _: Boolean, new: Boolean ->
        ignoreSync = true

        when (new) {
            true -> inputElement.focusInput()
            else -> inputElement.blur      ()
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

            if ((textField.placeHolderColor ?: textField.placeHolderFont) != null) {
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

            style.cursor = when (textField.cursor) {
                null -> ""
                else -> "inherit"
            }

            style.caretColor = when {
                textField.cursorVisible -> ""
                else                    -> "transparent"
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

            spellcheck = spellCheck

            accessibilityManager?.linkNativeElement(textField, this)
        }

        maskChanged(textField, textField.mask, textField.mask)
        updatePurpose(textField)

        eventHandler = eventHandlerFactory(inputElement, this).apply {
            registerKeyListener      ()
            registerFocusListener    ()
            registerClickListener    ()
            registerInputListener    ()
            registerSelectionListener()
        }

        textField.apply {
            textChanged         += this@NativeTextField.textChanged
            maskChanged         += this@NativeTextField.maskChanged
            focusChanged        += this@NativeTextField.focusChanged
            styleChanged        += this@NativeTextField.styleChanged
            boundsChanged       += this@NativeTextField.boundsChanged
            purposeChanged      += this@NativeTextField.purposeChanged
            enabledChanged      += this@NativeTextField.enabledChanged
            selectionChanged    += this@NativeTextField.selectionChanged
            focusabilityChanged += this@NativeTextField.focusabilityChanged
        }

        if (textField.hasFocus) {
            mobileKeyboardManager.show()
        }
    }

    fun discard() {
        textField.apply {
            textChanged         -= this@NativeTextField.textChanged
            maskChanged         -= this@NativeTextField.maskChanged
            focusChanged        -= this@NativeTextField.focusChanged
            styleChanged        -= this@NativeTextField.styleChanged
            boundsChanged       -= this@NativeTextField.boundsChanged
            purposeChanged      -= this@NativeTextField.purposeChanged
            enabledChanged      -= this@NativeTextField.enabledChanged
            selectionChanged    -= this@NativeTextField.selectionChanged
            focusabilityChanged -= this@NativeTextField.focusabilityChanged
        }

        selectionStyle    = null
        placeHolderStyle  = null
        mozSelectionStyle = null

        (inputElement.parentElement as? HTMLElement)?.style?.setProperty("transform-style", "")

        accessibilityManager?.unlinkNativeElement(textField, inputElement)
    }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(inputElement))
            (inputElement.parentElement as? HTMLElement)?.style?.setProperty("transform-style", "preserve-3d")
        }

        if (textField.hasFocus && !elementFocused) {
            inputElement.focusInput()

            // This forces the element to take focus, so only doing if it should be focused
            select(textField.selection.run { start .. end })
        }
    }

    fun fitTextSize() = textMetrics.size(textField.displayText, textField.font).run {
        val borderWidth  = if (textField.borderVisible) borderSize.width  else 2.0
        val borderHeight = if (textField.borderVisible) borderSize.height else 2.0

        val h = if (height == 0.0) textField.height - borderHeight else height

        Size(max(8.0, width) + 2 * borderWidth, max(0.0, h) + 2 * borderHeight)
    }

    override fun onKeyUp(event: Event) = true.also { syncTextField() }
    override fun onInput(event: Event) = true.also { syncTextField() }

    override fun onFocusGained(event: Event) = true.also {
        elementFocused = true

        mobileKeyboardManager.hide()

        if (!ignoreSync) {
            focusManager?.requestFocus(textField)
        }
    }

    override fun onFocusLost(event: Event) = true.also {
        elementFocused = false

        // HACK!!
        // Native text fields will lose focus when the user clicks outside the browser window,
        // this will result in focus being cleared BEFORE the FocusManagerImpl can record the
        // previously focused View to return focus to when focus comes back to the window.
        // This hack essentially tries to "check" whether the window lost focus as a "result" of this
        // focus loss. If so, it does not clear the focus.
        scheduler.now {
            if (!ignoreSync && focusManager?.focusOwner == textField) {
                focusManager.clearFocus()
            }
        }
    }

    override fun onSelect(event: Event) = true.also {
        textField.select(selection)
    }

    override fun onClick(event: Event) = true.also {
        textField.select(selection)
    }

    private fun select(range: ClosedRange<Int>) {
        try {
            // attempt to select, but handle case where input type doesn't support it
            inputElement.setSelectionRange(range.start, range.endInclusive)
        } catch (ignore: Throwable) {

        }
    }

    private fun syncTextField() {
        if (ignoreSync) {
            return
        }

        // Ignore updates from the TextField during this call since
        // they will invalidate the underlying selection range
        // by modifying the start/end position. They are also
        // redundant.

        textField.textChanged      -= textChanged
        textField.selectionChanged -= selectionChanged

        textField.text = text

        textField.textChanged      += textChanged
        textField.selectionChanged += selectionChanged

        if (textField.text != text) {
            text = textField.text
        }
    }

    private fun updatePurpose(textField: TextField) {
        inputElement.type = when (textField.purpose) {
            Password        -> PASSWORD
            Email           -> "email"
            Search          -> "search"
            Telephone       -> "tel"
            Number, Integer -> "number"
            Url             -> "url"
            else            -> when (textField.mask) {
                null -> "text"
                else -> PASSWORD
            }
        }

        if (textField.purpose == Integer) {
            inputElement.pattern = "[0-9]*"
        }
    }

    private companion object {
        const val PASSWORD = "password"
    }
}