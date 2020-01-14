package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Icon
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.BorderStyle.None
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Static
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setBorderStyle
import com.nectar.doodle.dom.setFont
import com.nectar.doodle.dom.setHeight
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setLeft
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.dom.setTop
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.Anchor
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.HorizontalAlignment.Right
import com.nectar.doodle.utils.VerticalAlignment.Bottom
import com.nectar.doodle.utils.VerticalAlignment.Middle
import com.nectar.doodle.utils.VerticalAlignment.Top
import org.w3c.dom.HTMLElement
import kotlin.math.max
import kotlin.math.min


interface NativeButtonFactory {
    operator fun invoke(button: Button): NativeButton
}

class NativeButtonFactoryImpl internal constructor(
        private val textMetrics              : TextMetrics,
        private val textFactory              : TextFactory,
        private val htmlFactory              : HtmlFactory,
        private val graphicsSurfaceFactory   : RealGraphicsSurfaceFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?): NativeButtonFactory {
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
            Insets(height, width, height, width)
        }
    }

    private val buttonInsets: Insets by lazy {
        val block = htmlFactory.create<HTMLElement>().apply {
            style.setPosition(Static())

            add(htmlFactory.createText("foo"))
        }
        val button = htmlFactory.createButton().apply {
            textContent = "foo"
        }

        val s = elementRuler.size(block)

        button.add(block)

        val size = elementRuler.size(button)

        // TODO: Get values for each side properly
        (Size(size.width - s.width, size.height - s.height) / 2.0).run {
            Insets(height, width, height, width)
        }
    }
}

class NativeButton internal constructor(
        private val textMetrics           : TextMetrics,
        private val textFactory           : TextFactory,
        private val htmlFactory           : HtmlFactory,
        private val graphicsSurfaceFactory: RealGraphicsSurfaceFactory,
                    handlerFactory        : NativeEventHandlerFactory,
        private val focusManager          : FocusManager?,
        private val button                : Button,
        private val insets                : Insets,
        private val border                : Insets): NativeEventListener {

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

    init {
        nativeEventHandler = handlerFactory(buttonElement, this).apply {
            registerFocusListener         ()
            registerClickListener         ()
//            startConsumingMousePressEvents()
        }

        button.apply {
            textChanged         += this@NativeButton.textChanged
            focusChanged        += this@NativeButton.focusChanged
            enabledChanged      += this@NativeButton.enabledChanged
            focusabilityChanged += this@NativeButton.focusableChanged
        }

        setIconText()
    }

    fun discard() {
        button.apply {
            textChanged         -= this@NativeButton.textChanged
            focusChanged        -= this@NativeButton.focusChanged
            enabledChanged      -= this@NativeButton.enabledChanged
            focusabilityChanged -= this@NativeButton.focusableChanged
        }
    }

    private val textPosition: Point get() {
        val bounds     = button.bounds
        val stringSize = stringSize // cache
        var minX       = insets.left
        val minY       = insets.top
        var maxX       = bounds.width  - stringSize.width  - insets.right
        val maxY       = bounds.height - stringSize.height - insets.bottom

        icon?.let {
            when (button.iconAnchor) {
                Anchor.Left,  Anchor.Leading  -> minX += it.width + button.iconTextSpacing
                Anchor.Right, Anchor.Trailing -> maxX -= it.width + button.iconTextSpacing
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
            val minX         = insets.left
            val maxX         = button.width - it.width - insets.right
            val aStringWidth = stringSize.width

            x = when (button.iconAnchor) {
                Anchor.Leading       ->

                    if (aStringWidth > 0) {
                        max(minX, textPosition.x - it.width - button.iconTextSpacing)
                    } else {
                        max(minX, min(maxX, (button.width - it.width) / 2))
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
                        max(minX, min(maxX, (button.width - it.width) / 2))
                    }
                else -> x
            }

            y = when (button.verticalAlignment) {
                Bottom -> button.height - insets.bottom
                Middle -> max(insets.top, min(button.height - insets.bottom, (button.height - it.size.height) / 2))
                Top    -> insets.top
                else   -> insets.top
            }
        }

        return Point(x, y)
    }

    private val icon: Icon<Button>? get() {
        val model = button.model

        return when {
            !button.enabled -> if (model.selected) button.disabledSelectedIcon else button.disabledIcon
            model.pressed   -> button.pressedIcon
            model.selected  -> button.selectedIcon
            model.mouseOver -> if (model.selected) button.mouseOverSelectedIcon else button.mouseOverIcon
            else            -> button.icon
        }
    }

    private var lastIcon = icon
        set(value) {
            if (value !== field) {
                field = value

                iconElement?.let { buttonElement.remove(it) }

                field?.let {
                    iconElement = htmlFactory.create<HTMLElement>().also { iconElement ->

                        iconElement.style.setWidth (it.size.width )
                        iconElement.style.setHeight(it.size.height)

                        val canvas = graphicsSurfaceFactory.surface(iconElement).canvas

                        canvas.size = it.size

                        it.render(button, canvas, Origin)

                        buttonElement.insert(iconElement, 0)
                    }

                    return
                }

                iconElement = null
            }
        }

    fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {
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

    override fun onFocusGained(): Boolean {
        if (!button.focusable) {
            return false
        }

        focusManager?.requestFocus(button)

        return true
    }

    override fun onFocusLost(): Boolean {
        if (button === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }

        return true
    }

    private fun measureIdealSize(): Size {
        val stringSize = stringSize // cache

        var width  = stringSize.width
        var height = stringSize.height

        icon?.let {
            width  += button.iconTextSpacing + it.size.width
            height  = max(it.size.height, height)
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