package io.nacular.doodle.drawing.impl

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Visible
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setCursor
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.NativeFocusManager
import io.nacular.doodle.geometry.Size
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.events.EventTarget

/**
 * Created by Nicholas Eddy on 12/7/19.
 */
internal interface NativeHyperLinkFactory {
    operator fun invoke(hyperLink: HyperLink): NativeHyperLink
}

internal class NativeHyperLinkFactoryImpl internal constructor(
        private val textMetrics              : TextMetrics,
        private val htmlFactory              : HtmlFactory,
        private val nativeEventHandlerFactory: NativeEventHandlerFactory,
        private val focusManager             : FocusManager?,
        private val nativeFocusManager       : NativeFocusManager?
): NativeHyperLinkFactory {
    override fun invoke(hyperLink: HyperLink) = NativeHyperLink(
            textMetrics,
            htmlFactory,
            nativeEventHandlerFactory,
            focusManager,
            nativeFocusManager,
            hyperLink)
}

class NativeHyperLink internal constructor(
        private val textMetrics           : TextMetrics,
                    htmlFactory           : HtmlFactory,
                    handlerFactory        : NativeEventHandlerFactory,
        private val focusManager          : FocusManager?,
        private val nativeFocusManager    : NativeFocusManager?,
        private val hyperLink             : HyperLink): NativeEventListener {

    var idealSize: Size? = null
        private set

    private val nativeEventHandler: NativeEventHandler

    private val linkElement = htmlFactory.create<HTMLAnchorElement>("a").apply {
        href = hyperLink.url

        style.setFont         (hyperLink.font           )
        style.setColor        (hyperLink.foregroundColor)
        style.setCursor       (hyperLink.cursor         )
        style.setWidthPercent (100.0                    )
        style.setHeightPercent(100.0                    )
        style.setOverflow     (Visible()                )
    }

    private val textChanged: (View, String, String) -> Unit = { _,_,_ ->
        hyperLink.rerender()
    }

    private val focusChanged: (View, Boolean, Boolean) -> Unit = { _,_,new ->
        when (new) {
            true -> linkElement.focus()
            else -> linkElement.blur ()
        }

        nativeFocusManager?.hasFocusOwner = new
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

    private val stringSize get() = textMetrics.size(hyperLink.text, hyperLink.font)

    fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
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