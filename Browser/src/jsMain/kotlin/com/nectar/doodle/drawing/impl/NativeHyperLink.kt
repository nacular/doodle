package com.nectar.doodle.drawing.impl

import com.nectar.doodle.controls.buttons.HyperLink
import com.nectar.doodle.core.View
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setFont
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Insets
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventTarget

/**
 * Created by Nicholas Eddy on 12/7/19.
 */
internal interface NativeHyperLinkFactory {
    operator fun invoke(hyperLink: HyperLink): NativeHyperLink
}

internal class NativeHyperLinkFactoryImpl internal constructor(
        private val textMetrics              : TextMetrics,
        private val textFactory              : TextFactory,
        private val htmlFactory              : HtmlFactory,
        private val graphicsSurfaceFactory   : RealGraphicsSurfaceFactory,
        private val elementRuler             : ElementRuler,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?): NativeHyperLinkFactory {
    override fun invoke(hyperLink: HyperLink) = NativeHyperLink(
            textMetrics,
            textFactory,
            htmlFactory,
            graphicsSurfaceFactory,
            nativeEventHandlerFactory,
            focusManager,
            hyperLink,
            Insets.None,
            Insets.None)

//    private val buttonBorder: Insets by lazy {
//        val button = htmlFactory.createButton().also {
//            it.textContent = "foo"
//        }
//
//        val s = elementRuler.size(button)
//
//        button.style.setBorderStyle(BorderStyle.None())
//
//        val size = elementRuler.size(button)
//
//        // TODO: Get values for each side properly
//        (Size(s.width - size.width, s.height - size.height) / 2.0).run {
//            Insets(height, width, height, width)
//        }
//    }
//
//    private val buttonInsets: Insets by lazy {
//        val block = htmlFactory.create<HTMLElement>().apply {
//            style.setPosition(Static())
//
//            add(htmlFactory.createText("foo"))
//        }
//        val button = htmlFactory.createButton().apply {
//            textContent = "foo"
//        }
//
//        val s = elementRuler.size(block)
//
//        button.add(block)
//
//        val size = elementRuler.size(button)
//
//        // TODO: Get values for each side properly
//        (Size(size.width - s.width, size.height - s.height) / 2.0).run {
//            Insets(height, width, height, width)
//        }
//    }
}

class NativeHyperLink internal constructor(
        private val textMetrics           : TextMetrics,
        private val textFactory           : TextFactory,
        private val htmlFactory           : HtmlFactory,
        private val graphicsSurfaceFactory: RealGraphicsSurfaceFactory,
                    handlerFactory        : NativeEventHandlerFactory,
        private val focusManager          : FocusManager?,
        private val hyperLink             : HyperLink,
        private val insets                : Insets,
        private val border                : Insets): NativeEventListener {

    var idealSize: Size? = null
        private set

    private var textElement       : HTMLElement? = null
//    private var iconElement       : HTMLElement? = null
    private val nativeEventHandler: NativeEventHandler

    private val linkElement = htmlFactory.create<HTMLAnchorElement>("a").apply {
        style.setFont         (null      )
//        style.setPosition     (Relative())
        style.setWidthPercent (100.0     )
        style.setHeightPercent(100.0     )
        style.cursor = "inherit"

        href = "javascript:void(0);"

//        disabled     = !hyperLink.enabled
    }

    private val textChanged: (View, String, String) -> Unit = { _,_,_ ->
        hyperLink.rerender()
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> linkElement.focus()
            else -> linkElement.blur ()
        }
    }

//    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
//        linkElement.disabled = !new
//    }

    private val focusableChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        linkElement.tabIndex = if (new) 0 else -1
    }

    init {
        nativeEventHandler = handlerFactory(linkElement, this).apply {
            registerFocusListener()
            registerClickListener()
        }

        hyperLink.apply {
            textChanged         += this@NativeHyperLink.textChanged
            focusChanged        += this@NativeHyperLink.focusChanged
//            enabledChanged      += this@NativeHyperLink.enabledChanged
            focusabilityChanged += this@NativeHyperLink.focusableChanged
        }

        setIconText()
    }

    fun discard() {
        hyperLink.apply {
            textChanged         -= this@NativeHyperLink.textChanged
            focusChanged        -= this@NativeHyperLink.focusChanged
//            enabledChanged      -= this@NativeHyperLink.enabledChanged
            focusabilityChanged -= this@NativeHyperLink.focusableChanged
        }
    }

//    private val textPosition: Point
//        get() {
//            val bounds     = hyperLink.bounds
//            val stringSize = stringSize // cache
//            var minX       = insets.left
//            val minY       = insets.top
//            var maxX       = bounds.width  - stringSize.width  - insets.right
//            val maxY       = bounds.height - stringSize.height - insets.bottom
//
////            icon?.let {
////                when (hyperLink.iconAnchor) {
////                    Anchor.Left,  Anchor.Leading  -> minX += it.width + hyperLink.iconTextSpacing
////                    Anchor.Right, Anchor.Trailing -> maxX -= it.width + hyperLink.iconTextSpacing
////                }
////            }
//
//            val x = when (hyperLink.horizontalAlignment) {
//                HorizontalAlignment.Right  -> maxX
//                HorizontalAlignment.Center -> max(minX, min(maxX, (bounds.width - stringSize.width) / 2)) - border.left
//                HorizontalAlignment.Left   -> minX
//                else                       -> minX
//            }
//
//            val y = when (hyperLink.verticalAlignment) {
//                VerticalAlignment.Bottom -> maxY
//                VerticalAlignment.Middle -> max(minY, min(maxY, (bounds.height - stringSize.height) / 2)) - border.top
//                VerticalAlignment.Top    -> minY
//                else                     -> minY
//            }
//
//            return Point(x, y)
//        }

    private val stringSize get() = textMetrics.size(hyperLink.text, hyperLink.font)

//    private val iconPosition: Point
//        get() {
//            var x = insets.left
//            var y = insets.top
//
//            icon?.let {
//                val minX         = insets.left
//                val maxX         = hyperLink.width - it.width - insets.right
//                val aStringWidth = stringSize.width
//
//                x = when (hyperLink.iconAnchor) {
//                    Anchor.Leading  ->
//
//                        if (aStringWidth > 0) {
//                            max(minX, textPosition.x - it.width - hyperLink.iconTextSpacing)
//                        } else {
//                            max(minX, min(maxX, (hyperLink.width - it.width) / 2))
//                        }
//
//                    Anchor.Right    ->
//
//                        if (aStringWidth > 0) {
//                            max(maxX, textPosition.x + aStringWidth + hyperLink.iconTextSpacing)
//                        } else {
//                            max(maxX, minX)
//                        }
//
//                    Anchor.Trailing ->
//
//                        if (aStringWidth > 0) {
//                            textPosition.x + aStringWidth + hyperLink.iconTextSpacing
//                        } else {
//                            max(minX, min(maxX, (hyperLink.width - it.width) / 2))
//                        }
//                    else            -> x
//                }
//
//                y = when (hyperLink.verticalAlignment) {
//                    VerticalAlignment.Bottom -> hyperLink.height - insets.bottom
//                    VerticalAlignment.Middle -> max(insets.top, min(hyperLink.height - insets.bottom, (hyperLink.height - it.size.height) / 2))
//                    VerticalAlignment.Top    -> insets.top
//                    else                     -> insets.top
//                }
//            }
//
//            return Point(x, y)
//        }

//    private val icon: Icon<Button>? get() {
//        val model = hyperLink.model
//
//        return when {
//            !hyperLink.enabled -> if (model.selected) hyperLink.disabledSelectedIcon else hyperLink.disabledIcon
//            model.pressed   -> hyperLink.pressedIcon
//            model.selected  -> hyperLink.selectedIcon
//            model.mouseOver -> if (model.selected) hyperLink.mouseOverSelectedIcon else hyperLink.mouseOverIcon
//            else            -> hyperLink.icon
//        }
//    }

//    private var lastIcon = icon
//        set(value) {
//            if (value !== field) {
//                field = value
//
//                iconElement?.let { linkElement.remove(it) }
//
//                field?.let {
//                    iconElement = htmlFactory.create<HTMLElement>().also { iconElement ->
//
//                        iconElement.style.setWidth (it.size.width )
//                        iconElement.style.setHeight(it.size.height)
//
//                        val canvas = graphicsSurfaceFactory.surface(iconElement).canvas
//
//                        canvas.size = it.size
//
//                        it.render(hyperLink, canvas, Point.Origin)
//
//                        linkElement.insert(iconElement, 0)
//                    }
//
//                    return
//                }
//
//                iconElement = null
//            }
//        }

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
//            if (lastIcon !== icon || text != hyperLink.text) {
//                setIconText()
//            }
//
//            positionElements()

            canvas.addData(listOf(linkElement))

            if (hyperLink.hasFocus) {
                linkElement.focus()
            }

            linkElement.style.setSize(hyperLink.size)
        }
    }

//    override fun onClick(): Boolean {
//        hyperLink.click()
//        return true
//    }

    override fun onFocusGained(target: EventTarget?): Boolean {
        if (!hyperLink.focusable) {
            return false
        }

        focusManager?.requestFocus(hyperLink)

        return true
    }

    override fun onFocusLost(target: EventTarget?): Boolean {
        if (hyperLink === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }

        return true
    }

    private fun measureIdealSize(): Size {
        val stringSize = stringSize // cache

        var width  = stringSize.width
        var height = stringSize.height

//        icon?.let {
//            width  += hyperLink.iconTextSpacing + it.size.width
//            height  = max(it.size.height, height)
//        }

        width  += insets.left + insets.right
        height += insets.top  + insets.bottom

        return Size(width, height)
    }

    private var text = hyperLink.text
        set(value) {
            if (value != field) {
                field = value

                linkElement.textContent = value
            }
        }

//    private fun positionElements() {
//        iconElement?.let {
//            iconPosition.apply {
//                it.style.setTop (y)
//                it.style.setLeft(x)
//            }
//        }
//
//        textElement?.let {
//            textPosition.apply {
//                it.style.setTop (y)
//                it.style.setLeft(x)
//            }
//        }
//    }

    private fun setIconText() {
        text      = hyperLink.text
        idealSize = measureIdealSize()
    }
}