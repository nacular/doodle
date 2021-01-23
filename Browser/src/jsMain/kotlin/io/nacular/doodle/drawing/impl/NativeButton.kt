package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.BorderStyle.None
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.insert
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.setBorderStyle
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setHeight
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setLeft
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setTop
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.div
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.VerticalAlignment.Bottom
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.VerticalAlignment.Top
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventTarget
import kotlin.math.max
import kotlin.math.min


internal interface NativeButtonFactory {
    operator fun invoke(button: Button): NativeButton
}

internal class NativeButtonFactoryImpl internal constructor(
        private val textMetrics              : TextMetrics,
        private val textFactory              : TextFactory,
        private val htmlFactory              : HtmlFactory,
        private val graphicsSurfaceFactory   : RealGraphicsSurfaceFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?
): NativeButtonFactory {

    override fun invoke(button: Button) = NativeButton(
            textMetrics,
            textFactory,
            htmlFactory,
            graphicsSurfaceFactory,
            nativeEventHandlerFactory,
            focusManager,
            button,
            buttonInsets,
            buttonBorder)

    private val buttonBorder: Insets by lazy {
        val button = htmlFactory.createButton().also {
            it.textContent = "foo"
        }

        val s = elementRuler.size(button)

        button.style.setBorderStyle(None())

        val size = elementRuler.size(button)

        // TODO: Get values for each side properly
        (Size(s.width - size.width, s.height - size.height) / 2.0).run {
            Insets(top = height, left = width, bottom = height, right = width)
        }
    }

    private val buttonInsets: Insets by lazy {
        val block = htmlFactory.create<HTMLElement>().apply {
            style.setPosition(Static())

            add(htmlFactory.createText("foo"))
        }

        val button = htmlFactory.createButton()

        val s = elementRuler.size(block)

        button.add(block)

        val size = elementRuler.size(button)

        // TODO: Get values for each side properly
        (Size(size.width - s.width, size.height - s.height) / 2.0).run {
            Insets(top    = height - buttonBorder.top,
                   left   = width  - buttonBorder.left,
                   bottom = height - buttonBorder.bottom,
                   right  = width  - buttonBorder.right)
        }
    }
}

internal class NativeButton internal constructor(
        private val textMetrics           : TextMetrics,
        private val textFactory           : TextFactory,
        private val htmlFactory           : HtmlFactory,
        private val graphicsSurfaceFactory: RealGraphicsSurfaceFactory,
                    handlerFactory        : NativeEventHandlerFactory,
        private val focusManager          : FocusManager?,
        private val button                : Button,
        private val insets                : Insets,
        private val border                : Insets
): NativeEventListener {

    var idealSize: Size? = null
        private set

    private var textElement       : HTMLElement? = null
    private var iconElement       : HTMLElement? = null
    private val nativeEventHandler: NativeEventHandler

    private val buttonElement = htmlFactory.createButton().apply {
        style.setFont         (null      )
//        style.setPosition     (Relative())
        style.setWidthPercent (100.0     )
        style.setHeightPercent(100.0     )
        style.cursor = "inherit"
        disabled     = !button.enabled
    }

    private val textChanged: (View, String, String) -> Unit = { _,_,_ ->
        button.rerender()
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> buttonElement.focus()
            else -> buttonElement.blur ()
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        buttonElement.disabled = !new
    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        buttonElement.tabIndex = if (new) 0 else -1
    }

    private val textPosition: Point get() {
        val bounds     = button.bounds
        val stringSize = stringSize // cache
        var minX       = insets.left
        val minY       = insets.top
        var maxX       = bounds.width  - stringSize.width  - insets.right
        val maxY       = bounds.height - stringSize.height - insets.bottom

        icon?.let {
            val size = it.size(button)

            when (button.iconAnchor) {
                Anchor.Left,  Anchor.Leading  -> minX += size.width + button.iconTextSpacing
                Anchor.Right, Anchor.Trailing -> maxX -= size.width + button.iconTextSpacing
            }
        }

        val x = when (button.horizontalAlignment) {
            Right  -> maxX
            Center -> max(minX, min(maxX, (bounds.width - stringSize.width) / 2)) - border.left
            Left   -> minX
            else   -> minX
        }

        val y = when (button.verticalAlignment) {
            Bottom -> maxY
            Middle -> max(minY, min(maxY, (bounds.height - stringSize.height) / 2)) - border.top
            Top    -> minY
            else   -> minY
        }

        return Point(x, y)
    }

    private val stringSize get() = textMetrics.size(button.text, button.font)

    private val iconPosition: Point get() {
        var x = insets.left
        var y = insets.top

        icon?.let {
            val size         = it.size(button)
            val minX         = insets.left
            val maxX         = button.width - size.width - insets.right
            val aStringWidth = stringSize.width

            x = when (button.iconAnchor) {
                Anchor.Leading       ->

                    if (aStringWidth > 0) {
                        max(minX, textPosition.x - size.width - button.iconTextSpacing)
                    } else {
                        max(minX, min(maxX, (button.width - size.width) / 2))
                    }

                Anchor.Right         ->

                    if (aStringWidth > 0) {
                        max(maxX, textPosition.x + aStringWidth + button.iconTextSpacing)
                    } else {
                        max(maxX, minX)
                    }

                Anchor.Trailing ->

                    if (aStringWidth > 0) {
                        textPosition.x + aStringWidth + button.iconTextSpacing
                    } else {
                        max(minX, min(maxX, (button.width - size.width) / 2))
                    }
                else -> x
            }

            y = when (button.verticalAlignment) {
                Bottom -> button.height - insets.bottom
                Middle -> max(insets.top, min(button.height - insets.bottom, (button.height - size.height) / 2))
                Top    -> insets.top
                else   -> insets.top
            }
        }

        return Point(x, y)
    }

    private val icon: Icon<Button>? get() {
        val model = button.model

        return when {
            !button.enabled   -> if (model.selected) button.disabledSelectedIcon else button.disabledIcon
            model.pressed     -> button.pressedIcon
            model.selected    -> button.selectedIcon
            model.pointerOver -> if (model.selected) button.pointerOverSelectedIcon else button.pointerOverIcon
            else              -> button.icon
        }
    }

    private var lastIcon: Icon<Button>? = null
        set(value) {
            if (value !== field) {
                field = value

                iconElement?.let { buttonElement.remove(it) }

                field?.let {
                    iconElement = htmlFactory.create<HTMLElement>().also { iconElement ->
                        val size = it.size(button)

                        iconElement.style.setWidth (size.width )
                        iconElement.style.setHeight(size.height)

                        val canvas = graphicsSurfaceFactory(iconElement).canvas

                        canvas.size = size

                        it.render(button, canvas, Origin)

                        buttonElement.insert(iconElement, 0)
                    }

                    return
                }

                iconElement = null
            }
        }

    init {
        nativeEventHandler = handlerFactory(buttonElement, this).apply {
            registerFocusListener()
            registerClickListener()
        }

        button.apply {
            textChanged         += this@NativeButton.textChanged
            focusChanged        += this@NativeButton.focusChanged
            enabledChanged      += this@NativeButton.enabledChanged
            focusabilityChanged += this@NativeButton.focusableChanged
        }

        lastIcon = icon

        setIconText()
    }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            if (lastIcon !== icon || text != button.text) {
                setIconText()
            }

            positionElements()

            canvas.addData(listOf(buttonElement))

            if (button.hasFocus) {
                buttonElement.focus()
            }

            buttonElement.style.setSize(button.size)
        }
    }

//    override fun onClick(): Boolean {
//        button.click()
//        return true
//    }

    override fun onFocusGained(target: EventTarget?): Boolean {
        if (!button.focusable) {
            return false
        }

        focusManager?.requestFocus(button)

        return true
    }

    override fun onFocusLost(target: EventTarget?): Boolean {
        if (button === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }

        return true
    }

    fun discard() {
        button.apply {
            textChanged         -= this@NativeButton.textChanged
            focusChanged        -= this@NativeButton.focusChanged
            enabledChanged      -= this@NativeButton.enabledChanged
            focusabilityChanged -= this@NativeButton.focusableChanged
        }
    }

    private fun measureIdealSize(): Size {
        val stringSize = stringSize // cache

        var width  = stringSize.width
        var height = stringSize.height

        icon?.let {
            val size = it.size(button)
            width  += button.iconTextSpacing + size.width
            height  = max(size.height, height)
        }

        width  += insets.left + insets.right
        height += insets.top  + insets.bottom

        return Size(width, height)
    }

    private var text = button.text
        set(value) {
            if (value != field) {
                field = value

                textElement?.let { buttonElement.remove(it) }

                textElement = if (field.isNotEmpty()) {
                    textFactory.create(field, button.font).also {
                        buttonElement.insert(it, 0)
                    }
                } else {
                    null
                }
            }
        }

    private fun positionElements() {
        iconElement?.let {
            iconPosition.apply {
                it.style.setTop (y)
                it.style.setLeft(x)
            }
        }

        textElement?.let {
            textPosition.apply {
                it.style.setTop (y)
                it.style.setLeft(x)
            }
        }
    }

    private fun setIconText() {
        text      = button.text
        idealSize = measureIdealSize()
    }
}