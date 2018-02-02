package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Icon
import com.nectar.doodle.dom.BorderStyle.None
import com.nectar.doodle.dom.Display.Inline
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Position.Absolute
import com.nectar.doodle.dom.Position.Static
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setBackgroundColor
import com.nectar.doodle.dom.setBorderStyle
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setFont
import com.nectar.doodle.dom.setHeight
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setLeft
import com.nectar.doodle.dom.setOpacity
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.dom.setTop
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.VerticalAlignment
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import kotlin.math.max
import kotlin.math.min


interface NativeButtonFactory {
    operator fun invoke(button: Button): NativeButton
}

class NativeButtonFactoryImpl internal constructor(
        private val textMetrics           : TextMetrics,
        private val textFactory           : TextFactory,
        private val htmlFactory           : HtmlFactory,
        private val graphicsSurfaceFactory: RealGraphicsSurfaceFactory,
        private val elementRuler          : ElementRuler,
        private val nativeEventHandler    : () -> NativeEventHandler): NativeButtonFactory {
    override fun invoke(button: Button) = NativeButton(textMetrics,
            textFactory,
            htmlFactory,
            graphicsSurfaceFactory,
            elementRuler,
            nativeEventHandler(),
            button)
}

class NativeButton internal constructor(
        private val textMetrics: TextMetrics,
        private val textFactory: TextFactory,
        private val htmlFactory: HtmlFactory,
        private val graphicsSurfaceFactory: RealGraphicsSurfaceFactory,
        private val elementRuler: ElementRuler,
                    nativeEventHandler: NativeEventHandler,
        private val button: Button) : NativeEventListener /*, PropertyListener,*/ {

    var idealSize: Size? = null
        private set

    private var textElement      : HTMLElement? = null
    private var iconElement      : HTMLElement? = null
    private val buttonElement    : HTMLButtonElement
    private val glassPanelElement: HTMLElement
    private val insets           : Insets
    private val border           : Insets

    init {
        insets = calculateButtonInsets()
        border = calculateButtonBorder()

        println("Insets: $insets")

        buttonElement = htmlFactory.createButton().apply {
            style.setFont         (null )
            style.setWidthPercent (100.0)
            style.setHeightPercent(100.0)

            nativeEventHandler.registerFocusListener    (this)
            nativeEventHandler.registerClickListener    (this)
            nativeEventHandler.startConsumingMouseEvents(this)

            nativeEventHandler += this@NativeButton
        }

        glassPanelElement = htmlFactory.create().apply {
            style.setTop            (0.0      )
            style.setLeft           (0.0      )
            style.setOpacity        (0f       )
            style.setPosition       (Absolute )
            style.setWidthPercent   (100.0    )
            style.setHeightPercent  (100.0    )
            style.setBackgroundColor(Color.red)

            buttonElement.add(this)
        }

        button.apply {
            textChanged      += ::textChanged
            focusChanged     += ::focusChanged
            enabledChanged   += ::enabledChanged
            focusableChanged += ::focusChanged
        }

        setIconText()
    }

    private val textPosition: Point get() {
        val bounds     = button.bounds
        val stringSize = stringSize // cache
        val minX       = insets.left
        val minY       = insets.top
        val maxX       = bounds.width  - stringSize.width  - insets.right
        val maxY       = bounds.height - stringSize.height - insets.bottom

//        if (icon != null) {
//            when (button.iconAnchor()) {
//                LEFT, LEADING   -> minX += aIcon.width + button.getIconTextSpacing()
//                RIGHT, TRAILING -> maxX -= aIcon.width + button.getIconTextSpacing()
//            }
//        }

        val x = when (button.horizontalAlignment) {
            HorizontalAlignment.Right  -> maxX
            HorizontalAlignment.Center -> max(minX, min(maxX, (bounds.width - stringSize.width) / 2)) - border.left
            HorizontalAlignment.Left   -> minX
            else                       -> minX
        }

        val y = when (button.verticalAlignment) {
            VerticalAlignment.Bottom  -> maxY
            VerticalAlignment.Center  -> max(minY, min(maxY, (bounds.height - stringSize.height) / 2)) - border.top
            VerticalAlignment.Top     -> minY
            else                      -> minY
        }

        return Point(x, y)
    }

    private val stringSize get() = textMetrics.size(button.text, button.font)

    private val iconPosition: Point get() {
        val x = insets.left
        var y = insets.top

        icon?.let {
//            val minX         = padding.left
//            val maxX         = button.width - icon.width - padding.right
//            val aStringWidth = stringSize.width
//
//            when (button.getIconAnchor()) {
//                LEADING  ->
//
//                    x = if (aStringWidth > 0) {
//                        max(minX, aStringPosition.getX() - aIcon.width - button.getIconTextSpacing())
//                    } else {
//                        max(minX, min(maxX, (aBounds.width - aIcon.width) / 2))
//                    }
//
//                RIGHT    ->
//
//                    x = if (aStringWidth > 0) {
//                        max(maxX, aStringPosition.getX() + aStringWidth + button.getIconTextSpacing())
//                    } else {
//                        max(maxX, minX)
//                    }
//
//                TRAILING ->
//
//                    x = if (aStringWidth > 0) {
//                        aStringPosition.getX() + aStringWidth + button.getIconTextSpacing()
//                    } else {
//                        max(minX, min(maxX, (aBounds.width - aIcon.width) / 2))
//                    }
//
//                LEFT     -> {
//                }
//            }

            y = when (button.verticalAlignment) {
                VerticalAlignment.Bottom -> button.height - insets.bottom
                VerticalAlignment.Center -> max(insets.top, min(button.height - insets.bottom, (button.height - it.size.height) / 2))
                VerticalAlignment.Top    -> insets.top
                else                     -> insets.top
            }
        }

        return Point(x, y)
    }

    private val icon: Icon<Button>? get() {
        val model = button.model

        return if (!button.enabled) {
            if (model.selected) button.disabledSelectedIcon else button.disabledIcon
        } else if (model.pressed) {
            button.pressedIcon
        } else if (model.selected) {
            button.selectedIcon
        } else if (model.mouseOver) {
            if (model.selected) button.mouseOverSelectedIcon else button.mouseOverIcon
        } else {
            button.icon
        }
    }

    private var lastIcon = icon
        set(value) {
            if (value !== field) {
                field = value

                iconElement?.let { buttonElement.remove(it) }

                field?.let {
                    iconElement = htmlFactory.create().also { iconElement ->

                        iconElement.style.setWidth (it.size.width )
                        iconElement.style.setHeight(it.size.height)

                        val canvas = graphicsSurfaceFactory.surface(iconElement).canvas

                        canvas.size = it.size

                        it.render(button, canvas, Point.Origin)

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

            updateIconPosition()
            updateTextPosition()

            canvas.addData(listOf(buttonElement))
        }
    }

    fun uninstall(button: Button) {
        button.apply {
            textChanged      -= ::textChanged
            focusChanged     -= ::focusChanged
            enabledChanged   -= ::enabledChanged
            focusableChanged -= ::focusableChanged
        }
    }

//    fun propertyChanged(aEvent: PropertyEvent) {
//        if (aEvent.getProperty() === Button.ICON_ANCHOR) {
//            button.rerender()
//        }
//    }

    override fun onClick(): Boolean {
        button.click()
        return true
    }

    override fun onFocusGained(): Boolean {
        if (!button.focusable) {
            return false
        }

//        focusManager.requestFocus(button)

        return true
    }

    override fun onFocusLost(): Boolean {
//        if (button === focusManager.focusOwner) {
//            focusManager.clearFocus()
//        }

        return true
    }

    @Suppress("UNUSED_PARAMETER")
    private fun textChanged(gizmo: Gizmo, old: String, new: String) {
        button.rerender()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun focusChanged(gizmo: Gizmo, old: Boolean, new: Boolean) {
        when (new) {
            true -> buttonElement.focus()
            else -> buttonElement.blur ()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun enabledChanged(gizmo: Gizmo, old: Boolean, new: Boolean) {
        buttonElement.disabled = !new
    }

    @Suppress("UNUSED_PARAMETER")
    private fun focusableChanged(gizmo: Gizmo, old: Boolean, new: Boolean) {
        buttonElement.tabIndex = if (new) -1 else 0
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

                textElement = if (!field.isEmpty()) {
                    textFactory.create(field, button.font).also {
                        buttonElement.insert(it, 0)
                    }
                } else {
                    null
                }
            }
        }

    private fun updateTextPosition() {
        textElement?.let {
            textPosition.apply {
                it.style.setTop (y)
                it.style.setLeft(x)
            }
        }
    }

    private fun updateIconPosition() {
        iconElement?.let {
            iconPosition.apply {
                it.style.setTop (y)
                it.style.setLeft(x)
            }
        }
    }

    private fun setIconText() {
        text      = button.text
        idealSize = measureIdealSize()
    }

    private fun calculateButtonBorder(): Insets {
        val button = htmlFactory.createButton().also {
            it.textContent = "foo"
        }

        val s = elementRuler.size(button)

        button.style.setBorderStyle(None)

        val size = elementRuler.size(button)

        println("Border size: ${s - size}")

        // TODO: Get values for each side properly
        return ((s - size) / 2).run {
            Insets(height, width, height, width)
        }
    }

    private fun calculateButtonInsets(): Insets {
        val block  = htmlFactory.create      ()
        val button = htmlFactory.createButton().also {
            it.textContent = "foo"
        }

        block.style.setDisplay (Inline )
        block.style.setPosition(Static)

        block.add(htmlFactory.createText("foo"))

        val s = elementRuler.size(block)

        button.add(block)

        val size = elementRuler.size(button)

        // TODO: Get values for each side properly
        return ((size - s) / 2).run {
            Insets(height, width, height, width)
        }
    }
}