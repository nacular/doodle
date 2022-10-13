package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Visible
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Size
import org.w3c.dom.HTMLAnchorElement

/**
 * Created by Nicholas Eddy on 12/7/19.
 */
internal interface NativeHyperLinkFactory {
    operator fun invoke(hyperLink: HyperLink, customRenderer: ((HyperLink, Canvas) -> Unit)? = null): NativeHyperLink
}

internal class NativeHyperLinkFactoryImpl internal constructor(
        private val textMetrics              : TextMetrics,
        private val htmlFactory              : HtmlFactory,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val canvasFactory            : CanvasFactory,
        private val focusManager             : FocusManager?
): NativeHyperLinkFactory {
    override fun invoke(hyperLink: HyperLink, customRenderer: ((HyperLink, Canvas) -> Unit)?) = NativeHyperLink(
            textMetrics,
            htmlFactory,
            nativeEventHandlerFactory,
            focusManager,
            canvasFactory,
            customRenderer,
            hyperLink)
}

internal class NativeHyperLink internal constructor(
        private val textMetrics   : TextMetrics,
                    htmlFactory   : HtmlFactory,
                    handlerFactory: NativeEventHandlerFactory,
        private val focusManager  : FocusManager?,
        private val canvasFactory : CanvasFactory,
        private val customRenderer: ((HyperLink, Canvas) -> Unit)?,
        private val hyperLink     : HyperLink): NativeEventListener {

    var idealSize: Size? = null
        private set

    private val nativeEventHandler: NativeEventHandler

    private val customCanvas: Canvas?

    private val linkElement = htmlFactory.create<HTMLAnchorElement>("a").apply {
        href = hyperLink.url

        if (customRenderer == null) {
            style.cursor = "inherit"
            style.setFont (hyperLink.font           )
            style.setColor(hyperLink.foregroundColor)
        }

        style.setOverflow     (Visible())
        style.setWidthPercent (100.0    )
        style.setHeightPercent(100.0    )

        customCanvas = if (customRenderer!= null) canvasFactory(this) else null
    }

    private val textChanged: (View, String, String) -> Unit = { _,_,_ ->
        hyperLink.rerender()
    }

    private val styleChanged: (View) -> Unit = {
        linkElement.style.setFont (it.font           )
        linkElement.style.setColor(it.foregroundColor)
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> linkElement.focus()
            else -> linkElement.blur ()
        }
    }

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        linkElement.href = when {
            new  -> hyperLink.url
            else -> ""
        }
    }

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
            enabledChanged      += this@NativeHyperLink.enabledChanged
            focusabilityChanged += this@NativeHyperLink.focusableChanged

            if (customRenderer == null) styleChanged += this@NativeHyperLink.styleChanged
        }

        setIconText()
    }

    fun discard() {
        nativeEventHandler.apply {
            unregisterFocusListener()
            unregisterClickListener()
        }

        hyperLink.apply {
            textChanged         -= this@NativeHyperLink.textChanged
            focusChanged        -= this@NativeHyperLink.focusChanged
            enabledChanged      -= this@NativeHyperLink.enabledChanged
            focusabilityChanged -= this@NativeHyperLink.focusableChanged

            if (customRenderer == null) styleChanged -= this@NativeHyperLink.styleChanged
        }
    }

    private val stringSize get() = textMetrics.size(hyperLink.text, hyperLink.font)

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            customCanvas?.also { customCanvas ->
                customCanvas.size = canvas.size
                customCanvas.clear()
                customRenderer?.invoke(hyperLink, customCanvas)
                customCanvas.flush()
            }

            canvas.addData(listOf(linkElement))

            if (hyperLink.hasFocus) {
                linkElement.focus()
            }

            linkElement.style.setSize(hyperLink.size)
        }
    }

    override fun onClick(event: Event) = true.also {
        if (isKeyboardClick(event)) {
            hyperLink.click()
        }
    }

    override fun onFocusGained(event: Event): Boolean {
        if (!hyperLink.focusable) {
            return false
        }

        focusManager?.requestFocus(hyperLink)

        return true
    }

    override fun onFocusLost(event: Event): Boolean {
        if (hyperLink === focusManager?.focusOwner) {
            focusManager.clearFocus()
        }

        return true
    }

    private fun measureIdealSize() = stringSize

    private var text = hyperLink.text
        set(value) {
            if (value != field) {
                field = value

                linkElement.textContent = value
            }
        }

    private fun setIconText() {
        text      = hyperLink.text
        idealSize = measureIdealSize()
    }
}