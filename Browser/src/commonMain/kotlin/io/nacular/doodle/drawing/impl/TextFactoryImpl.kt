package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Inline
import io.nacular.doodle.dom.Static
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.clear
import io.nacular.doodle.dom.numChildren
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setDisplay
import io.nacular.doodle.dom.setDomPosition
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setLineHeight
import io.nacular.doodle.dom.setTextAlignment
import io.nacular.doodle.dom.setTextDecoration
import io.nacular.doodle.dom.setTextIndent
import io.nacular.doodle.dom.setTextSpacing
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.text.Style
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.TextAlignment

internal class TextFactoryImpl(private val htmlFactory: HtmlFactory): TextFactory {
    override fun create(text: String, font: Font?, textSpacing: TextSpacing, possible: HTMLElement?): HTMLElement {
        val element = htmlFactory.createOrUse(TEXT_ELEMENT, possible)

        if (element.textContent != text) {
            element.textContent = text
        }

        font?.let {
            element.style.setFont(it)
        }

        element.style.setTextSpacing(textSpacing)

        if (element == possible) {
            element.style.setTextDecoration(null)
            element.style.whiteSpace = ""
            element.style.textIndent = ""
        }

        return element
    }

    override fun create(text: StyledText, textSpacing: TextSpacing, possible: HTMLElement?): HTMLElement {
        if (text.count == 1) {
            text.first().also { (text, style) ->
                return create(text, style.font, textSpacing, possible).also {
                    applyStyle(it, style)
                }
            }
        }

        val element = htmlFactory.createOrUse("B", possible)

        element.clear()
        element.style.width = ""
        element.style.setBackgroundColor(null)

        text.forEach { (text, style) ->
            element.add(create(text, style.font, textSpacing).also { element ->
                element.style.setDisplay    (Inline()   )
                element.style.setDomPosition(Static()   )
                element.style.setTextSpacing(textSpacing)

                applyStyle(element, style)
            })
        }

        return element
    }

    override fun wrapped(
        text       : String,
        font       : Font?,
        width      : Double,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing,
        possible   : HTMLElement?
    ) = wrapped(
        text        = text,
        font        = font,
        indent      = indent,
        alignment   = alignment,
        lineSpacing = lineSpacing,
        textSpacing = textSpacing,
        possible    = possible
    ).also {
        it.style.setWidth(width)
    }

    override fun wrapped(text: String, font: Font?, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement?) = create(
        text        = text,
        font        = font,
        possible    = possible,
        textSpacing = textSpacing,
    ).also {
        it.style.setLineHeight   (lineSpacing)
        it.style.setTextAlignment(alignment  )
        it.style.setTextSpacing  (textSpacing)
        applyWrap(it, indent)
    }

    override fun wrapped(text: StyledText, width: Double, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement?) = wrapped(
        text        = text,
        indent      = indent,
        possible    = possible,
        alignment   = alignment,
        lineSpacing = lineSpacing,
        textSpacing = textSpacing,
    ).also {
        it.style.setWidth(width)
    }

    override fun wrapped(text: StyledText, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing, possible: HTMLElement?) = create(
        text        = text,
        possible    = possible,
        textSpacing = textSpacing,
    ).also {
        it.style.setLineHeight   (lineSpacing)
        it.style.setTextAlignment(alignment  )
        it.style.setTextSpacing  (textSpacing)

        if (it.nodeName.equals(TEXT_ELEMENT, ignoreCase = true)) {
            applyWrap(it, indent)
        } else {
            (0 until it.numChildren).map { i -> it.childAt(i) }.filterIsInstance<HTMLElement>().forEach { applyWrap(it, indent) }
        }
    }

    private fun applyWrap(element: HTMLElement, indent: Double) {
        with(element.style) {
            whiteSpace = "pre-line"
            setTextIndent(indent)
        }
    }

    private fun applyStyle(element: HTMLElement, style: Style) {
        style.foreground?.let {
            when (it) {
                is ColorPaint -> element.style.setColor(it.color)
                else          -> { /* TODO: Implement */ }
            }
        } ?: run {
            element.style.setColor(null)
        }

        style.background?.let {
            when (it) {
                is ColorPaint -> element.style.setBackgroundColor(it.color)
                else          -> { /* TODO: Implement */ }
            }
        } ?: run {
            element.style.setBackgroundColor(null)
        }

        element.style.setTextDecoration(style.decoration)
    }

    private companion object {
        private const val TEXT_ELEMENT = "PRE"
    }
}